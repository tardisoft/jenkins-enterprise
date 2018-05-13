package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.JUnitGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class JUnitGoalSpec extends Specification {

    def "test createConfig default"() {
        setup:
        JUnitGoal step = new JUnitGoal()

        when:
        def config = step.createConfig()

        then:
        config == [
                testDataPublishers: [[$class: 'AttachmentPublisher']],
                testResults       : '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml',
                allowEmptyResults : true
        ]
    }

    def "test run"() {
        setup:
        JUnitGoal step = new JUnitGoal()
        JenkinsScript jenkins = Spy(JenkinsScript)

        step.testDataPublishers = 'hello'
        step.testResults = 'results'
        step.allowEmptyResult = false

        when:
        step.run(jenkins)

        then:
        1 * jenkins.junit([
                testDataPublishers: 'hello',
                testResults       : 'results',
                allowEmptyResults : false
        ])
    }

    def "test setupBuild"() {
        setup:
        JUnitGoal step = new JUnitGoal()
        JenkinsScript jenkins = Spy(JenkinsScript)
        MavenBuild build = new MavenBuild()

        when:
        step.setupBuild(jenkins, build)

        then:
        build.goals.contains('verify')
    }
}
