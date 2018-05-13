package io.tardisoft.jenkins.pipeline.maven.step.deploy

import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenGithubPagesDeployStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification


class MavenGithubPagesDeployStepSpec extends Specification {
    def "test run"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        MavenGithubPagesDeployStep deployStep = new MavenGithubPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository site:site')
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository site:stage')
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P deploy-github-page -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS} site-deploy')
            0 * script.sh(_)
        }
    }

    def "test run on wrong branch"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'PR-0'
        MavenGithubPagesDeployStep deployStep = new MavenGithubPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            0 * script.sh(_)
        }
    }
}
