package io.tardisoft.jenkins.pipeline.maven.step.deploy

import groovy.json.JsonOutput
import io.tardisoft.jenkins.pipeline.maven.build.goal.MavenBuildGoal
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import io.tardisoft.jenkins.pipeline.step.deploy.Deploy
import io.tardisoft.jenkins.pipeline.util.Common
import org.apache.commons.lang.StringUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Step to deploy maven artifacts to artifactory
 */
class MavenDeployStep extends MavenStep implements Serializable, Deploy {
    /**
     * The root pom to use, such as a aggregator or the base build point, usually just pom.xml
     */
    String rootPom = 'pom.xml'
    /**
     * The parent pom to use, may be the same as the root pom
     */
    String parentPom = 'pom.xml'
    /**
     * List of release branches
     */
    List<String> releaseBranches = []
    /**
     * Jenkins secrets ID of the Git credentials
     */
    String gitCredentialsId = 'gituser'
    /**
     * Jenkins secrets ID of the artifactory credentials
     */
    String artifactoryCredentialsId = 'artifactory'
    /**
     * True if snapshots should be deployed to artifactory
     */
    boolean deploySnapshots = true
    /**
     * Skip updates to associated pull request
     */
    boolean skipGitHubPullRequestComment = false
    /**
     * Github context to use for pull requests
     */
    String gitHubContext = 'continuous-integration/jenkins/pr-merge'
    /**
     * URL to github API, used for pull request updates
     * GHE would be something like https://github.mycompany.com/api/v3
     */
    String gitHubApiUrl = 'https://api.github.com'
    /**
     * Skip printing usage information at end of build
     */
    boolean skipPrintUsage = false
    /**
     * Prefix for tags
     */
    String tagPrefix = ''
    /**
     * Goals that where called during the build,  we use these to ensure we don't repeat anything during the deploy phase
     * such as tests, static code analysis etc.
     */
    List<MavenBuildGoal> mavenGoals = []
    /**
     * Skip creating a GitHub release
     */
    boolean skipGithubRelease = false

    /**
     * @deprecated Deprecated in favor of the {@link MavenGithubPagesDeployStep} does nothing now, will be removed in future
     */
    @Deprecated
    boolean deploySite

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        for (MavenBuildGoal g : mavenGoals) {
            g.setupDeploy(script, this)
        }

        boolean isReleaseBranch = new Common(script).isReleaseBranch(releaseBranches)
        if (isReleaseBranch) {
            deployRelease(script)
        } else {
            deploySnapshot(script)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def deploySnapshot(def script) {
        if (deploySnapshots) {
            script.withCredentials([script.usernamePassword(credentialsId: artifactoryCredentialsId, usernameVariable: 'MAVEN_USER', passwordVariable: 'MAVEN_PASS')]) {
                List<String> args = []
                if (StringUtils.isNotBlank(rootPom)) {
                    args.add("-f ${rootPom}")
                }
                args.add("deploy")
                args.add("-nsu")
                def url = script.scm.userRemoteConfigs?.first()?.url
                if (url != null) {
                    args.add("-DdeveloperConnectionUrl=scm:git:${url}")
                    args.add("-DconnectionUrl=scm:git:${url}")
                }
                mvn(script, args)
            }
            printUsage(script)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getTagString(def script) {
        def pom = script.readMavenPom(file: parentPom)
        return "${tagPrefix ?: ''}${pom.version}"
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def deployRelease(def script) {
        String releaseNotes = createReleaseNotes(script)

        script.withCredentials([script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
            script.withCredentials([script.usernamePassword(credentialsId: artifactoryCredentialsId, usernameVariable: 'MAVEN_USER', passwordVariable: 'MAVEN_PASS')]) {
                def url = script.scm.userRemoteConfigs.first().url
                List<String> args = []
                if (StringUtils.isNotBlank(rootPom)) {
                    args.add("-f ${rootPom}")
                }
                args.add("scm:tag")
                args.add("-nsu")
                args.add("-Dusername=\${GIT_USER}")
                args.add("-Dpassword=\${GIT_PASS}")
                args.add("-Dtag=${getTagString(script)}")
                if (url != null) {
                    args.add("-DdeveloperConnectionUrl=scm:git:${url}")
                    args.add("-DconnectionUrl=scm:git:${url}")
                }
                mvn script, args

                args = []
                if (StringUtils.isNotBlank(rootPom)) {
                    args.add("-f ${rootPom}")
                }
                args.add("deploy")
                args.add("-nsu")
                mvn script, args
            }
        }

        def pom = script.readMavenPom(file: parentPom)
        updatePullRequest(script, pom)
        createGithubRelease(script, pom, releaseNotes)

        printUsage(script)
    }

    /**
     * Print the usage information to the build log and pull request
     * @param script Jenkinsfile script context
     */
    def printUsage(def script) {
        if (skipPrintUsage) {
            return
        }
        def pom = script.readMavenPom(file: parentPom)
        printMavenUsage(script, pom)
        printUsageFiles(script)
    }

    /**
     * Print out contents of any usage file artifacts
     * @param script Jenkinsfile script context
     */
    def printUsageFiles(def script) {
        script.sh('find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\'')
    }

    protected void printMavenUsage(def script) {
        def pom = script.readMavenPom(file: parentPom)
        printMavenUsage(script, pom)
    }
    /**
     * Print out the basic usage information
     * @param script Jenkinsfile script context
     */
    protected void printMavenUsage(def script, def pom) {
        if (pom == null) {
            return
        }
        script.echo "Usage:\n<dependency>\n    <groupId>${pom.groupId}</groupId>\n    <artifactId>${pom.artifactId}</artifactId>\n    <version>${pom.version}</version>\n</dependency>"
    }

    /**
     * Update the associated Github pull request
     * @param script Jenkinsfile script context
     */
    def updatePullRequest(def script) {
        def pom = script.readMavenPom(file: parentPom)
        updatePullRequest(script, pom)
    }

    /**
     * Update the associated Github pull request
     * @param script Jenkinsfile script context
     */
    def updatePullRequest(def script, def pom) {
        if (skipGitHubPullRequestComment || pom == null) {
            return
        }
        boolean isPullRequest = new Common(script).isPullRequest()
        if (!isPullRequest) {
            return
        }
        String usageMsg = "[${script.currentBuild.absoluteUrl}](${script.currentBuild.absoluteUrl})\\r\\nBuild Number: [${script.currentBuild.displayName - "#"}](${script.currentBuild.absoluteUrl})\\r\\nBuild Duration: ${script.currentBuild.durationString - 'and counting'}\\r\\n\\r\\nUsage:\\r\\n```xml\\r\\n<dependency>\\r\\n    <groupId>${pom.groupId}</groupId>\\r\\n    <artifactId>${pom.artifactId}</artifactId>\\r\\n    <version>${pom.version}</version>\\r\\n</dependency>\\r\\n```"
        String url = script.scm.userRemoteConfigs.first().url as String
        def rootUrl = gitHubApiUrl - 'api/v3'
        def path = url - rootUrl - '.git'
        try {
            String branch = new Common(script).getBranch()
            def prNum = branch - "PR-"
            def postUrl = "${gitHubApiUrl}/repos/${path}/issues/${prNum}/comments"
            script.withCredentials([script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                script.sh("""curl -H "Content-Type: application/json" -H "Authorization: token \${GIT_PASS}" --data '{"body": "${
                    usageMsg
                }"}' ${postUrl}""")
            }
        } catch (Exception e) {
            StringWriter stacktrace = new StringWriter()
            e.printStackTrace(new PrintWriter(stacktrace))
            script.echo "Error setting github pull request comment: ${e.getMessage()}\n${stacktrace.toString()}"
        }
    }

    def createReleaseNotes(def script) {
        if (skipGithubRelease
                || !new Common(script).isReleaseBranch(releaseBranches)
        ) {
            return ""
        }
        String releaseNotes = null
        try {
            releaseNotes = script.sh(
                    script: 'git log $(git describe --tags --abbrev=0)..HEAD --pretty=format:" * (%t) %s" --no-merges',
                    returnStdout: true)
        } catch (Exception e) {
            StringWriter stacktrace = new StringWriter()
            e.printStackTrace(new PrintWriter(stacktrace))
            script.echo "Error generating release notes: ${e.getMessage()}\n${stacktrace.toString()}"
        }

        if (StringUtils.isBlank(releaseNotes)) {
            releaseNotes = ""
        }

        // Remove all quotes to avoid problems with the curl command
        releaseNotes = releaseNotes.replaceAll("\"", "")
        releaseNotes = releaseNotes.replaceAll("'", "")
        return releaseNotes
    }

    def createGithubRelease(def script, def pom, String releaseNotes = null) {
        if (skipGithubRelease
                || pom == null
                || !new Common(script).isReleaseBranch(releaseBranches)
        ) {
            return
        }

        String msg = "Release:\n\tGroup ID: ${pom.groupId}\n\tArtifact ID: ${pom.artifactId}\n\tVersion: ${pom.version}"
        if (StringUtils.isNotBlank(releaseNotes)) {
            msg = "${msg}\n\nNotes:\n${releaseNotes}"
        }
        msg = msg.replace("\n", "\r\n")
        def json = JsonOutput.toJson([
                "tag_name"  : "${tagPrefix}${pom.version}",
                "name"      : pom.version,
                "body"      : msg,
                "draft"     : false,
                "prerelease": false
        ])
        String url = script.scm.userRemoteConfigs.first().url as String
        def uri = url.replaceAll('\\.git$', "")
        Matcher matcher = Pattern.compile("(https?://)([^:^/]*)(:\\d*)?(.*)?").matcher(uri)
        def name = ""
        if (matcher.find()) {
            name = matcher.group(4)
        } else if (uri.contains("@")) {
            name = "/" + uri.split("@")[1].split(":")[1]
        }
        def postUrl = "${gitHubApiUrl}/repos${name}/releases"
        script.withCredentials([script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
            script.sh("""curl -H "Content-Type: application/json" -H "Authorization: token \${GIT_PASS}" --data '${
                json
            }' ${postUrl}""")
        }
    }
}
