package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.goal.CoberturaGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class CoberturaGoalSpec extends Specification {

    def "test createConfig default"() {
        setup:
        CoberturaGoal step = new CoberturaGoal()

        when:
        def config = step.createConfig()

        then:
        config == [
                $class             : 'CoberturaPublisher',
                autoUpdateHealth   : false,
                autoUpdateStability: false,
                coberturaReportFile: ' **/target/site/cobertura/coverage.xml',
                failUnhealthy      : false,
                failUnstable       : false,
                maxNumberOfBuilds  : 0,
                onlyStable         : false,
                sourceEncoding     : 'UTF_8',
                zoomCoverageChart  : false
        ]
    }

    def "test run bad override"() {
        setup:
        CoberturaGoal step = new CoberturaGoal() {
            @Override
            def createConfig() {
                def val = super.createConfig()
                val['$class'] = 'bad'
                return val
            }
        }
        JenkinsScript jenkins = Mock(JenkinsScript)

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'CoberturaPublisher',
                autoUpdateHealth   : false,
                autoUpdateStability: false,
                coberturaReportFile: ' **/target/site/cobertura/coverage.xml',
                failUnhealthy      : false,
                failUnstable       : false,
                maxNumberOfBuilds  : 0,
                onlyStable         : false,
                sourceEncoding     : 'UTF_8',
                zoomCoverageChart  : false
        ])
    }

    def "test run"() {
        setup:
        CoberturaGoal step = new CoberturaGoal()
        JenkinsScript jenkins = Mock(JenkinsScript)

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'CoberturaPublisher',
                autoUpdateHealth   : false,
                autoUpdateStability: false,
                coberturaReportFile: ' **/target/site/cobertura/coverage.xml',
                failUnhealthy      : false,
                failUnstable       : false,
                maxNumberOfBuilds  : 0,
                onlyStable         : false,
                sourceEncoding     : 'UTF_8',
                zoomCoverageChart  : false
        ])
    }
}
