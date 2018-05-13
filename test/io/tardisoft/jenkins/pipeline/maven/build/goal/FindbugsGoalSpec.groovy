package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.FindBugsGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class FindbugsGoalSpec extends Specification {

    def "test default config"() {
        setup:
        FindBugsGoal step = new FindBugsGoal()

        when:
        def config = step.createConfig()

        then:
        config['$class'] == 'FindBugsPublisher'
        config['canComputeNew'] == false
        config['defaultEncoding'] == 'UTF-8'
        config['pattern'] == '**/findbugsXml.xml'
        config['shouldDetectModules'] == true
        config['excludePattern'] == ''
        config['failedTotalAll'] == '0'
        config['healthy'] == ''
        config['includePattern'] == ''
        config['unHealthy'] == ''
    }

    def "test config"() {
        setup:
        FindBugsGoal step = new FindBugsGoal()
        JenkinsScript jenkins = new JenkinsScript()

        when:
        step.canComputeNew = canComputeNew
        step.defaultEncoding = defaultEncoding
        step.pattern = pattern
        step.shouldDetectModules = shouldDetectModules
        step.failedTotalAll = failedTotalAll
        step.healthy = healthy
        step.excludePattern = excludePattern
        step.includePattern = includePattern
        step.unHealthy = unHealthy
        def config = step.createConfig()

        then:
        config['$class'] == 'FindBugsPublisher'
        config['canComputeNew'] == canComputeNew
        config['defaultEncoding'] == defaultEncoding
        config['pattern'] == pattern
        config['shouldDetectModules'] == shouldDetectModules
        config['failedTotalAll'] == failedTotalAll
        config['healthy'] == healthy
        config['excludePattern'] == excludePattern
        config['includePattern'] == includePattern
        config['unHealthy'] == unHealthy

        where:
        canComputeNew | defaultEncoding | pattern                    | shouldDetectModules | excludePattern | failedTotalAll | healthy | includePattern | unHealthy
        true          | 'CP12'          | 'checkstyle.xml'           | false               | ''             | '12'           | '0'     | 'high'         | '0'
        false         | 'UTF-8'         | '**/checkstyle-result.xml' | true                | 'ae'           | '0'            | ''      | 'normal'       | ''

    }

    def "test override config"() {
        setup:
        JenkinsScript jenkins = Spy(JenkinsScript)
        FindBugsGoal step = new FindBugsGoal() {
            @Override
            def createConfig() {
                def config = super.createConfig()
                config['$class'] = 'BadValue'
                return config
            }
        }

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'FindBugsPublisher',
                canComputeNew      : step.canComputeNew,
                defaultEncoding    : step.defaultEncoding,
                excludePattern     : step.excludePattern,
                failedTotalAll     : step.failedTotalAll,
                healthy            : step.healthy,
                includePattern     : step.includePattern,
                pattern            : step.pattern,
                shouldDetectModules: step.shouldDetectModules,
                unHealthy          : step.unHealthy,
                thresholdLimit     : step.thresholdLimit
        ])
    }

    def "test run"() {
        setup:
        JenkinsScript jenkins = Mock(JenkinsScript)
        FindBugsGoal step = new FindBugsGoal()

        when:
        step.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'FindBugsPublisher',
                canComputeNew      : step.canComputeNew,
                defaultEncoding    : step.defaultEncoding,
                excludePattern     : step.excludePattern,
                failedTotalAll     : step.failedTotalAll,
                healthy            : step.healthy,
                includePattern     : step.includePattern,
                pattern            : step.pattern,
                shouldDetectModules: step.shouldDetectModules,
                unHealthy          : step.unHealthy,
                thresholdLimit     : step.thresholdLimit
        ])
    }

    def "test setupBuild"() {
        setup:
        MavenBuild build = new MavenBuild()
        FindBugsGoal step = new FindBugsGoal()

        when:
        step.setupBuild(null, build)

        then:
        !build.goals.contains("findbugs:findbugs -Dfindbugs.skip=false")
    }

}