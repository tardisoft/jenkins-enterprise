package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.goal.JacocoGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class JacocoGoalSpec extends Specification {

    def "test createConfig default"() {
        setup:
        JacocoGoal step = new JacocoGoal()

        when:
        def config = step.createConfig()

        then:
        config == [
                '$class': 'JacocoPublisher',
        ]
    }

    def "test run bad override"() {
        setup:
        JacocoGoal step = new JacocoGoal() {
            @Override
            def createConfig() {
                def val = super.createConfig()
                val['$class'] = 'bad'
                return val
            }
        }
        JenkinsScript jenkins = Spy(JenkinsScript)

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                '$class': 'JacocoPublisher',
        ])
    }

    def "test run"() {
        setup:
        JacocoGoal step = new JacocoGoal()
        JenkinsScript jenkins = Spy(JenkinsScript)

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                '$class': 'JacocoPublisher',
        ])
    }
}
