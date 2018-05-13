package io.tardisoft.jenkins.pipeline.step

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class PropertiesStepSpec extends Specification {

    def "test create Config"() {
        setup:
        JenkinsScript jenkins = Spy(JenkinsScript)
        PropertiesStep step = new PropertiesStep()
        step.artifactDaysToKeepStr = '3'
        step.artifactNumToKeepStr = '10'
        step.daysToKeepStr = '22'
        step.numToKeepStr = '50'

        when:
        def config = step.createConfig(jenkins)

        then:
        config == ['buildDiscarder(logRotator([artifactDaysToKeepStr:3, artifactNumToKeepStr:10, daysToKeepStr:22, numToKeepStr:50]))', 'disableConcurrentBuilds()']
    }

    def "test run default"() {
        setup:
        JenkinsScript jenkins = Spy(JenkinsScript)
        PropertiesStep step = new PropertiesStep()

        when:
        step.run(jenkins)

        then:
        1 * jenkins.properties([
                'buildDiscarder(logRotator([artifactDaysToKeepStr:, artifactNumToKeepStr:3, daysToKeepStr:, numToKeepStr:25]))',
                'disableConcurrentBuilds()'
        ])
    }
}
