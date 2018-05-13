package io.tardisoft.jenkins.pipeline.maven.step

import io.tardisoft.jenkins.pipeline.maven.step.QualityGateStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class QualityGateStepSpec extends Specification {

    JenkinsScript jenkins = Spy(JenkinsScript)

    def setup() {
        jenkins.scm.userRemoteConfigs = [[url: 'https://github.myorg.com/child-org/foo-application.git']]
    }

    def "test run default"() {
        setup:
        QualityGateStep step = new QualityGateStep()
        String shArg = ''

        when:
        step.run(jenkins)

        then:
        1 * jenkins.withSonarQubeEnv('Sonar', _ as Closure)
        1 * jenkins.sh({ args -> shArg = args })

        and:
        shArg == 'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.repository=child-org/foo-application -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com'
    }

    def "test run pull request"() {
        setup:
        jenkins.env = [CHANGE_ID: '123']
        QualityGateStep step = new QualityGateStep()
        String shArg = ''

        when:
        step.run(jenkins)

        then:
        1 * jenkins.withSonarQubeEnv('Sonar', _ as Closure)
        1 * jenkins.sh({ args -> shArg = args })

        and:
        shArg == 'export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.login=XXXX -Dsonar.github.repository=child-org/foo-application -Dsonar.github.pullRequest=123 -Dsonar.analysis.mode=preview -Dsonar.github.oauth=YYYY -Dsonar.host.url=http://sonarqube.mycorp.com -Dsonar.github.endpoint=https://api.github.com'
    }
}
