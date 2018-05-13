package io.tardisoft.jenkins.pipeline.step

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class CleanStepSpec extends Specification {

    def "test createConfig default"() {
        setup:
        CleanStep cleanStep = new CleanStep()
        JenkinsScript jenkins = new JenkinsScript()

        when:
        def config = cleanStep.createConfig(jenkins)

        then:
        config == [
                '$class'      : 'WsCleanup',
                'notFailBuild': true
        ]
    }

    def "test run bad override"() {
        setup:
        CleanStep cleanStep = new CleanStep() {
            @Override
            def createConfig(Object script) {
                def val = super.createConfig(script)
                val['$class'] = 'bad'
                return val
            }
        }
        JenkinsScript jenkins = Mock(JenkinsScript)

        when:
        cleanStep.run(jenkins)

        then:
        1 * jenkins.step([
                '$class'      : 'WsCleanup',
                'notFailBuild': true
        ])
    }

    def "test run"() {
        setup:
        CleanStep cleanStep = new CleanStep()
        cleanStep.notFailBuild = false
        JenkinsScript jenkins = Mock(JenkinsScript)

        when:
        cleanStep.run(jenkins)

        then:
        1 * jenkins.step([
                '$class'      : 'WsCleanup',
                'notFailBuild': false
        ])
    }
}
