package io.tardisoft.jenkins.pipeline.maven.step.deploy

import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenDeployStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification
import spock.lang.Unroll

class MavenDeploySpec extends Specification {

    def "test skip deploy snapshot"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'feature-branch'
        MavenDeployStep step = new MavenDeployStep()
        step.releaseBranches = ['master']
        step.deploySnapshots = false

        when:
        step.run(script)

        then:
        0 * script.sh(_)
    }

    def "test deploy snapshot"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'feature-branch'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.parentPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true
        String shArg = ''

        when:
        step.run(script)

        then:
        1 * script.withCredentials(_, _)
        1 * script.sh({ args ->
            shArg = args
        })

        then:
        shArg == 'mvn -f pom.xml deploy -nsu -DdeveloperConnectionUrl=scm:git:git@localhost -DconnectionUrl=scm:git:git@localhost'
    }

    def "test deploy snapshot mvnArgs"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'feature-branch'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.parentPom = 'pom.xml'
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true
        String shArg = ''

        when:
        step.run(script)

        then:
        1 * script.withCredentials(_, _)
        1 * script.sh({ args ->
            shArg = args
        })

        then:
        shArg == 'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -f pom.xml deploy -DdeveloperConnectionUrl=scm:git:git@localhost -DconnectionUrl=scm:git:git@localhost'
    }

    def "test deploy release"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true
        step.skipGithubRelease = true
        List shArg = []

        when:
        step.run(script)

        then:
        2 * script.withCredentials(_, _)
        _ * script.sh({ args ->
            shArg.add(args)
        })

        then:
        shArg.size() == 2
        shArg[0] == 'mvn -f pom.xml scm:tag -nsu -Dusername=${GIT_USER} -Dpassword=${GIT_PASS} -Dtag=1.0.1-SNAPSHOT -DdeveloperConnectionUrl=scm:git:git@localhost -DconnectionUrl=scm:git:git@localhost'
        shArg[1] == 'mvn -f pom.xml deploy -nsu'
    }

    def "test deploy release with usage"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost:foo/bar.git']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = false
        step.skipGitHubPullRequestComment = true
        step.skipGithubRelease = false
        List shArg = []

        when:
        step.run(script)

        then:
        3 * script.withCredentials(_, _)
        1 * script.sh([script: 'git log $(git describe --tags --abbrev=0)..HEAD --pretty=format:" * (%t) %s" --no-merges', returnStdout: true]) >> " * (ADADAD) Refactor: Commit \"Message\"\n * (ADADAB) US1235: Commit 'Message2'\n * (ADADAC) Refactor: HelloWorld"
        _ * script.sh({ args ->
            shArg.add(args)
        })

        then:
        shArg == [
                'mvn -f pom.xml scm:tag -nsu -Dusername=${GIT_USER} -Dpassword=${GIT_PASS} -Dtag=1.0.1-SNAPSHOT -DdeveloperConnectionUrl=scm:git:git@localhost:foo/bar.git -DconnectionUrl=scm:git:git@localhost:foo/bar.git',
                'mvn -f pom.xml deploy -nsu',
                'curl -H "Content-Type: application/json" -H "Authorization: token ${GIT_PASS}" --data \'{"tag_name":"1.0.1-SNAPSHOT","name":"1.0.1-SNAPSHOT","body":"Release:\\r\\n\\tGroup ID: com.myorg.test\\r\\n\\tArtifact ID: testProj\\r\\n\\tVersion: 1.0.1-SNAPSHOT\\r\\n\\r\\nNotes:\\r\\n * (ADADAD) Refactor: Commit Message\\r\\n * (ADADAB) US1235: Commit Message2\\r\\n * (ADADAC) Refactor: HelloWorld","draft":false,"prerelease":false}\' https://api.github.com/repos/foo/bar/releases',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        ]

    }

    def "test deploy release mvnArgs"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true
        step.skipGithubRelease = true
        List shArg = []

        when:
        step.run(script)

        then:
        2 * script.withCredentials(_, _)
        _ * script.sh({ args ->
            shArg.add(args)
        })

        then:
        shArg.size() == 2
        shArg[0] == 'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -f pom.xml scm:tag -Dusername=${GIT_USER} -Dpassword=${GIT_PASS} -Dtag=1.0.1-SNAPSHOT -DdeveloperConnectionUrl=scm:git:git@localhost -DconnectionUrl=scm:git:git@localhost'
        shArg[1] == 'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -f pom.xml deploy'
    }

    def "test printUsage"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = false
        step.skipGitHubPullRequestComment = true
        String echoStr = ''

        when:
        step.printUsage(script)

        then:
        1 * script.echo({ args ->
            echoStr = args
        })

        then:
        echoStr == """Usage:
<dependency>
    <groupId>com.myorg.test</groupId>
    <artifactId>testProj</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>"""
    }

    def "test update pull request"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        def pom = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.poms['pom.xml'] = pom
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'PR-1'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = false
        step.skipGitHubPullRequestComment = false
        script.currentBuild.displayName = 'PR-1 #12'
        script.currentBuild.durationString = '5m 12s and counting'
        script.currentBuild.absoluteUrl = 'http://localhost/build12'
        String shArg = ''

        when:
        step.updatePullRequest(script, pom)

        then:
        1 * script.scm
        1 * script.withCredentials(_, _)
        1 * script.sh({ args ->
            shArg = args
        })

        then:
        shArg == """curl -H "Content-Type: application/json" -H "Authorization: token \${GIT_PASS}" --data '{"body": "[http://localhost/build12](http://localhost/build12)\\r\\nBuild Number: [PR-1 12](http://localhost/build12)\\r\\nBuild Duration: 5m 12s \\r\\n\\r\\nUsage:\\r\\n```xml\\r\\n<dependency>\\r\\n    <groupId>com.myorg.test</groupId>\\r\\n    <artifactId>testProj</artifactId>\\r\\n    <version>1.0.1-SNAPSHOT</version>\\r\\n</dependency>\\r\\n```"}' https://api.github.com/repos/git@localhost/issues/1/comments"""
    }

    @Unroll
    def "test release github curl for #url #repo"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        def pom = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1-SNAPSHOT'
        ]
        script.poms['pom.xml'] = pom
        script.scm.userRemoteConfigs = [['url': url]]
        script.env.BRANCH_NAME = 'PR-1'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = false
        step.skipGitHubPullRequestComment = false
        script.currentBuild.displayName = 'PR-1 #12'
        script.currentBuild.durationString = '5m 12s and counting'
        script.currentBuild.absoluteUrl = 'http://localhost/build12'
        String shArg = ''

        when:
        step.createGithubRelease(script, pom, "")

        then:
        1 * script.getEnv() >> [BRANCH_NAME: 'master']
        1 * script.withCredentials(_, _)
        1 * script.sh({ args ->
            shArg = args
        })

        then:
        shArg == """curl -H "Content-Type: application/json" -H "Authorization: token \${GIT_PASS}" --data '{"tag_name":"1.0.1-SNAPSHOT","name":"1.0.1-SNAPSHOT","body":"Release:\\r\\n\\tGroup ID: com.myorg.test\\r\\n\\tArtifact ID: testProj\\r\\n\\tVersion: 1.0.1-SNAPSHOT","draft":false,"prerelease":false}' https://api.github.com/repos/$repo/releases"""

        where:
        url                                              | repo
        "https://www.github.com/tardisoft/foo-repo"      | "tardisoft/foo-repo"
        "http://www.github.com/tardisoft1/foo-repo"      | "tardisoft1/foo-repo"
        "https://www.github.com/tardisoft2/foo-repo.git" | "tardisoft2/foo-repo"
        "git@github.com:tardisoft/micrometer-demo.git"   | "tardisoft/micrometer-demo"

    }

    def "test version tag with prefix"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1.RELEASE-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true
        step.tagPrefix = 'v'

        when:
        String version = step.getTagString(script)

        then:
        version == 'v1.0.1.RELEASE-SNAPSHOT'
    }

    def "test version tag"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1.RELEASE-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true

        when:
        String version = step.getTagString(script)

        then:
        version == '1.0.1.RELEASE-SNAPSHOT'
    }

    def "test version null prefix tag"() {
        setup:
        JenkinsScript script = Spy(JenkinsScript)
        script.poms['pom.xml'] = [
                groupId   : 'com.myorg.test',
                artifactId: 'testProj',
                version   : '1.0.1.RELEASE-SNAPSHOT'
        ]
        script.scm.userRemoteConfigs = [[url: 'git@localhost']]
        script.env.BRANCH_NAME = 'master'
        MavenDeployStep step = new MavenDeployStep()
        step.rootPom = 'pom.xml'
        step.jvmArgs = null
        step.tagPrefix = null
        step.mavenArgs = null
        step.localRepo = false
        step.releaseBranches = ['master']
        step.skipPrintUsage = true
        step.skipGitHubPullRequestComment = true

        when:
        String version = step.getTagString(script)

        then:
        version == '1.0.1.RELEASE-SNAPSHOT'
    }

}
