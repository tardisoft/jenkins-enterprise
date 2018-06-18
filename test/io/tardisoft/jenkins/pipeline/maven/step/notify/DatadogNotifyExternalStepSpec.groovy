package io.tardisoft.jenkins.pipeline.maven.step.notify

import hudson.PluginManager
import io.tardisoft.jenkins.pipeline.maven.MavenJenkinsPipeline
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import jenkins.model.Jenkins
import spock.lang.Specification


class DatadogNotifyExternalStepSpec extends Specification {
    JenkinsScript script = Spy(JenkinsScript)

    def setup() {
        script.scm.userRemoteConfigs = [[url: 'https://github.myorg.com/child-org/foo-application.git']]
    }

    def "test simple call with notify datadog on valid branch"() {
        setup:
        MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline()
        script.env.SKIP_VALIDATE_PLUGINS = true
        script.env.BRANCH_NAME = "master"
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.localRepo = true
        pipeline.notifyExternalStep = [new DatadogNotifyExternalStep()]
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
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.repository=child-org/foo-application -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com',
                'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dfindbugs.skip=true -Dcheckstyle.skip=true -DskipTests=true -Dmaven.test.skip=true -Djapicc.skip=true -Dsonar.skip=true -Dmaven.repo.local=$PWD/.repository -f pom.xml deploy -DdeveloperConnectionUrl=scm:git:https://github.myorg.com/child-org/foo-application.git -DconnectionUrl=scm:git:https://github.myorg.com/child-org/foo-application.git',
                'find . -name usage*.txt -print -exec cat \'{}\' \';\' -exec echo " " \';\'',
                'curl  -X POST -H "Content-type: application/json" \\\n' +
                        '    -d \'{\n' +
                        '          "title": "Application Build Event - foo-application",\n' +
                        '          "text": "Application was built.",\n' +
                        '          "priority": "normal",\n' +
                        '          "tags": ["org:child-org", "repo:foo-application", "environment:build", "branch:master"],\n' +
                        '          "alert_type": "info"\n' +
                        '    }\' \\\n' +
                        '    \'https://api.datadoghq.com/api/v1/events?api_key=${DD_API_KEY}\'\n' +
                        '    '
        ]
    }
}
