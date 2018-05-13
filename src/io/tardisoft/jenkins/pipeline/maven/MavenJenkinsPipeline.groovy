package io.tardisoft.jenkins.pipeline.maven

import io.tardisoft.jenkins.pipeline.AbstractJenkinsPipeline
import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.AbstractMavenGithubStep
import io.tardisoft.jenkins.pipeline.maven.step.ArchiveArtifactsStep
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import io.tardisoft.jenkins.pipeline.maven.step.MavenUpdateVersionStep
import io.tardisoft.jenkins.pipeline.maven.step.QualityGateStep
import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenDeployStep
import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenGithubPagesDeployStep
import io.tardisoft.jenkins.pipeline.maven.step.notify.AbstractNotifyExternalStep
import io.tardisoft.jenkins.pipeline.release.GitTagReleaseStrategy
import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Builds a maven based project
 * 1. Checks out code
 * 2. Updates the version to be either a release, or a branch build
 * 3. Run the build, tests, and static code analysis
 * 4. Publish the build results to Jenkins
 * 5. Run through additional Quality gate (aka Sonar)
 * 6. Tag and deploy the artifacts to artifactory
 * 7. Any final cleanup
 */
class MavenJenkinsPipeline extends AbstractJenkinsPipeline implements Serializable {

    /**
     * The parent pom of the project
     */
    String parentPom = 'pom.xml'
    /**
     * The root pom of the project
     */
    String rootPom = 'pom.xml'
    /**
     * Any Java Virtual Machine arguments to add to ALL the maven invocations
     */
    List<String> jvmArgs = []
    /**
     * Any additional maven arguments to add to ALL maven invocations
     */
    List<String> mavenArgs = []
    /**
     * Will deploy and download all artifacts to a maven repository relative to the local build
     */
    boolean localRepo = true
    /**
     * ID of the Jenkins Secret credentials to use for artifactory
     */
    String artifactoryCredentialsId = 'artifactory'
    /**
     * Prefix to use for tags
     */
    String tagPrefix = ''
    /**
     * True if quality gate should be invoked
     */
    boolean runQualityGate = true

    /**
     * Responsible for invoking the build phase
     */
    MavenBuild mavenBuildStep = new MavenBuild()
    /**
     * Responsible for updating the pom version for releases/snapshot builds
     */
    MavenUpdateVersionStep updateVersionStep = new MavenUpdateVersionStep(
            updateStrategy: new GitTagReleaseStrategy()
    )

    /**
     * List of steps to include in the publisher stage
     */
    List<Step> publishers = [
            new ArchiveArtifactsStep()
    ]
    /**
     * Deploys to artifacts to artifactory and prints out usage information and updates pull request with comment on build
     */
    MavenDeployStep Deploy = new MavenDeployStep()
    /**
     * if true: Deploy the maven site to the gh-pages (Github Pages) branch, default false
     */
    boolean deploySite = false
    /**
     * Runs quality metrics (aka Sonar)
     */
    QualityGateStep qualityGateStep = new QualityGateStep()

    /**
     * Runs quality metrics (aka Sonar)
     */
    List<AbstractNotifyExternalStep> notifyExternalStep = []
    /**
     * Deploys the Maven site to the gh-pages branch
     */
    List<AbstractMavenGithubStep> deploySteps = [new MavenGithubPagesDeployStep()]

    /**
     * {@inheritDoc}
     */
    @Override
    void buildAndTest(def script) {
        buildMaven(script)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void qualityGate(def script) {
        if (qualityGateStep == null || !runQualityGate) {
            return
        }

        addMavenConfig(qualityGateStep)

        qualityGateStep.run(script)
    }

    @Override
    void notifyExternal(Object script) {
        notifyExternalStep?.each {
            it.run(script)
        }
    }

    /**
     * Update the version of maven for this build.  This will change to version for releases, or snapshots
     * @param script Jenkinsfile script context
     */
    void updateVersion(def script) {
        if (updateVersionStep == null) {
            return
        }

        addMavenConfig(updateVersionStep)

        updateVersionStep.releaseBranches = releaseBranches
        updateVersionStep.parentPom = parentPom
        if (updateVersionStep.updateStrategy instanceof GitTagReleaseStrategy) {
            ((GitTagReleaseStrategy) updateVersionStep.updateStrategy).tagPrefix = tagPrefix
        }
        updateVersionStep.run(script)
    }

    /**
     * Root of the maven build process, includes setup steps and invoking the build
     * @param script Jenkinsfile script context
     */
    void buildMaven(def script) {
        updateVersion(script)
        buildMavenStep(script)
    }

    /**
     * Actually invoke the maven build
     * @param script Jenkinsfile script context
     */
    void buildMavenStep(def script) {
        if (mavenBuildStep == null) {
            return
        }

        addMavenConfig(mavenBuildStep)

        mavenBuildStep.rootPom = rootPom
        mavenBuildStep.run(script)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void publish(def script) {
        mavenBuildStep?.publish(script)
        for (Step step : publishers) {
            step.run(script)
        }
    }

    /**
     * Update the maven command with shared maven configuration options, such as jvm args, maven args etc.
     * @param step Jenkinsfile script context
     */
    void addMavenConfig(MavenStep step) {
        if (jvmArgs) {
            LinkedHashSet<String> mergeArgs = new LinkedHashSet<String>()
            mergeArgs.addAll(jvmArgs)
            mergeArgs.addAll(step.jvmArgs)
            step.jvmArgs = mergeArgs as List<String>
        }
        if (mavenArgs) {
            LinkedHashSet<String> mergeArgs = new LinkedHashSet<String>()
            mergeArgs.addAll(mavenArgs)
            mergeArgs.addAll(step.mavenArgs)
            step.mavenArgs = mergeArgs as List<String>
        }
        step.localRepo = localRepo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deploy(def script) {
        deployMaven(script)
    }

    @Override
    void site(def script) {
        deploySite(script)
    }

/**
 * Deploy the maven site, for example to github
 * @param script Jenkinsfile script context
 */
    void deploySite(def script) {
        if (!deploySite || !deploySteps) {
            return
        }

        deploySteps.each { deployStep ->

            setGitHubPullRequestStatus script, "Deploy step ${deployStep.getClass().simpleName}", 'PENDING'

            deployStep.mavenGoals.addAll(MavenBuild.mavenGoals)
            addMavenConfig(deployStep)
            deployStep.version = this.updateVersionStep.newVersion
            deployStep.gitCredentialsId = gitCredentialsId
            deployStep.releaseBranches = releaseBranches
            deployStep.run(script)
        }
    }

    /**
     * Deploy the maven artifacts and update related pull requests
     * @param script Jenkinsfile script context
     */
    void deployMaven(def script) {
        if (Deploy == null) {
            return
        }
        setGitHubPullRequestStatus script, "Deploying maven artifacts", 'PENDING'

        addMavenConfig(Deploy)

        Deploy.mavenGoals.addAll(mavenBuildStep.mavenGoals)

        Deploy.gitCredentialsId = gitCredentialsId
        Deploy.artifactoryCredentialsId = artifactoryCredentialsId
        Deploy.rootPom = rootPom
        Deploy.parentPom = parentPom
        Deploy.releaseBranches = releaseBranches
        Deploy.skipGitHubPullRequestComment = skipPullRequestStatus
        Deploy.tagPrefix = tagPrefix
        Deploy.run(script)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void cleanup(def script) {

    }
}
