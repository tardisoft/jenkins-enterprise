package io.tardisoft.jenkins.pipeline.maven

import hudson.PluginManager
import io.tardisoft.jenkins.pipeline.maven.MavenJenkinsPipeline
import io.tardisoft.jenkins.pipeline.maven.step.notify.AbstractNotifyExternalStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import io.tardisoft.jenkins.pipeline.util.Common
import jenkins.model.Jenkins
import spock.lang.Specification

import java.time.Duration

class MavenJenkinsPipelineSpec extends Specification {

    def "test simple call"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        _ * script.sh({ args -> shCalls.add(args) })

        then:
        shCalls == [
                'git config user.name Jenkins',
                'git config user.email defaultuser@nowhere.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -U -f pom.xml clean install verify -fae',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dfindbugs.skip=true -Dcheckstyle.skip=true -DskipTests=true -Dmaven.test.skip=true -Djapicc.skip=true -Dsonar.skip=true -Dmaven.repo.local=$PWD/.repository -f pom.xml deploy',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        ]
    }

    def "test simple call with default timeout"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true

        when:
        pipeline.run(script)

        then:
        1 * script.timeout(['time': 60, 'unit': 'MINUTES'], _)
    }

    def "test simple call with timeout"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        pipeline.buildTimeout = Duration.ofMinutes(120)

        when:
        pipeline.run(script)

        then:
        1 * script.timeout(['time': 120, 'unit': 'MINUTES'], _)
    }

    def "test simple call without timeout"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        pipeline.buildTimeout = null

        when:
        pipeline.run(script)

        then:
        0 * script.timeout(_, _)
    }

    def "test simple call with env"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        pipeline.env = ["hello": "world"]
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        1 * script.withEnv(pipeline.env, _)
        _ * script.sh({ args -> shCalls.add(args) })

        then:
        shCalls == [
                'git config user.name Jenkins',
                'git config user.email defaultuser@nowhere.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -U -f pom.xml clean install verify -fae',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dfindbugs.skip=true -Dcheckstyle.skip=true -DskipTests=true -Dmaven.test.skip=true -Djapicc.skip=true -Dsonar.skip=true -Dmaven.repo.local=$PWD/.repository -f pom.xml deploy',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        ]
    }

    def "test simple call with notify external on valid branch"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        script.env.BRANCH_NAME = "master"
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        pipeline.notifyExternalStep = [new JenkinsNotifyExternalStep(legacyJenkinsUrl: "http://foocom/build?TOKEN=abc123")]
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        _ * script.sh({ args ->
            shCalls.add(args)
        })

        then:
        shCalls == [
                'git config user.name Jenkins',
                'git config user.email defaultuser@nowhere.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -U -f pom.xml clean install verify -fae',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dfindbugs.skip=true -Dcheckstyle.skip=true -DskipTests=true -Dmaven.test.skip=true -Djapicc.skip=true -Dsonar.skip=true -Dmaven.repo.local=$PWD/.repository -f pom.xml deploy',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\'',
                'curl -X GET "http://foocom/build?TOKEN=abc123"'
        ]
    }

    def "test simple call with notify external not on valid branch"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        script.env.BRANCH_NAME = "PR-123"
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        pipeline.notifyExternalStep = [new JenkinsNotifyExternalStep(legacyJenkinsUrl: "http://foocom/build?TOKEN=abc123")]
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        _ * script.sh({ args ->
            println args
            shCalls.add(args)
        })

        then:
        shCalls == [
                'git config user.name Jenkins',
                'git config user.email defaultuser@nowhere.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -U -f pom.xml clean install verify -fae',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dfindbugs.skip=true -Dcheckstyle.skip=true -DskipTests=true -Dmaven.test.skip=true -Djapicc.skip=true -Dsonar.skip=true -Dmaven.repo.local=$PWD/.repository -f pom.xml deploy',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        ]
    }

    def "test call without qualityGate call"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.runQualityGate = false

        when:
        pipeline.run(script)

        then:
        0 * script.sh('mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=f479c58ad2a968872fe73ae7028bf6e7d1f07209 -Dsonar.github.oauth=bc140759fd6d36d0ba8ab4e51c6a4ca819c4e5ec -Dsonar.host.url=http://sonarqube.ose-elr-core.myorg.com -Dsonar.github.endpoint=https://api.github.com ')
    }

    def "test call with customization"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.jvmArgs.add('-Duser.time=CDT')
        pipeline.localRepo = false
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        _ * script.sh({ args -> shCalls.add(args) })

        then:
        verifyAll {
            shCalls.size() == 6
            shCalls[0] == 'git config user.name Jenkins'
            shCalls[1] == 'git config user.email defaultuser@nowhere.com'
            shCalls[2].contains('-Duser.time=CDT')
            shCalls[3].contains('-Duser.time=CDT')
            shCalls[4].contains('-Duser.time=CDT')
            shCalls[5] == 'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        }
    }

    def "test call with customization custom settings.xml"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.jvmArgs.add('-Duser.time=CDT')
        pipeline.localRepo = false
        List<String> shCalls = []

        when:
        pipeline.run(script)

        then:
        _ * script.sh({ args -> shCalls.add(args) })

        then:
        verifyAll {
            shCalls.size() == 6
            shCalls[0] == 'git config user.name Jenkins'
            shCalls[1] == 'git config user.email defaultuser@nowhere.com'
            shCalls[2].contains('-Duser.time=CDT')
            shCalls[3].contains('-Duser.time=CDT')
            shCalls[4].contains('-Duser.time=CDT')
            shCalls[5] == 'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\''
        }
    }

    class JenkinsNotifyExternalStep extends AbstractNotifyExternalStep {

        String legacyJenkinsUrl = ""

        @Override
        def run(def script) {
            if (new Common(script).isReleaseBranch(["master"]) && legacyJenkinsUrl) {
                notifyExternal(script)
            }
        }

        @Override
        def notifyExternal(def script) {
            script.sh("""curl -X GET \"$legacyJenkinsUrl\"""")
        }
    }
}
