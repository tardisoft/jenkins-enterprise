package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.CheckstyleGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class CheckstyleGoalSpec extends Specification {

    def "test default config"() {
        setup:
        CheckstyleGoal checkstyle = new CheckstyleGoal()

        when:
        def config = checkstyle.createConfig()

        then:
        config['$class'] == 'CheckStylePublisher'
        config['canComputeNew'] == false
        config['defaultEncoding'] == 'UTF-8'
        config['pattern'] == '**/checkstyle-result.xml'
        config['shouldDetectModules'] == true
        config['healthy'] == ''
        config['thresholdLimit'] == 'high'
        config['unHealthy'] == ''
    }

    def "test config"(boolean canComputeNew,
                      String defaultEncoding,
                      String pattern,
                      boolean shouldDetectModules,
                      String healthy,
                      String thresholdLimit,
                      String unHealthy) {
        setup:
        CheckstyleGoal checkstyle = new CheckstyleGoal()
        JenkinsScript jenkins = new JenkinsScript()

        when:
        checkstyle.canComputeNew = canComputeNew
        checkstyle.defaultEncoding = defaultEncoding
        checkstyle.pattern = pattern
        checkstyle.shouldDetectModules = shouldDetectModules
        checkstyle.healthy = healthy
        checkstyle.thresholdLimit = thresholdLimit
        checkstyle.unHealthy = unHealthy
        def config = checkstyle.createConfig()

        then:
        config['$class'] == 'CheckStylePublisher'
        config['canComputeNew'] == canComputeNew
        config['defaultEncoding'] == defaultEncoding
        config['pattern'] == pattern
        config['shouldDetectModules'] == shouldDetectModules
        config['healthy'] == healthy
        config['thresholdLimit'] == thresholdLimit
        config['unHealthy'] == unHealthy

        where:
        canComputeNew | defaultEncoding | pattern                    | shouldDetectModules | healthy | thresholdLimit | unHealthy
        true          | 'CP12'          | 'checkstyle.xml'           | false               | '0'     | 'high'         | '0'
        false         | 'UTF-8'         | '**/checkstyle-result.xml' | true                | ''      | 'normal'       | ''

    }

    def "test override config"() {
        setup:
        JenkinsScript jenkins = Mock(JenkinsScript)
        CheckstyleGoal checkstyle = new CheckstyleGoal() {
            @Override
            def createConfig() {
                def config = super.createConfig()
                config['$class'] = 'BadValue'
                return config
            }
        }

        when:
        checkstyle.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'CheckStylePublisher',
                canComputeNew      : checkstyle.canComputeNew,
                defaultEncoding    : checkstyle.defaultEncoding,
                pattern            : checkstyle.pattern,
                shouldDetectModules: checkstyle.shouldDetectModules,
                healthy            : checkstyle.healthy,
                thresholdLimit     : checkstyle.thresholdLimit,
                unHealthy          : checkstyle.unHealthy
        ])
    }

    def "test run"() {
        setup:
        JenkinsScript jenkins = Mock(JenkinsScript)
        CheckstyleGoal checkstyle = new CheckstyleGoal()

        when:
        checkstyle.run(jenkins)

        then:
        1 * jenkins.step([
                $class             : 'CheckStylePublisher',
                canComputeNew      : checkstyle.canComputeNew,
                defaultEncoding    : checkstyle.defaultEncoding,
                pattern            : checkstyle.pattern,
                shouldDetectModules: checkstyle.shouldDetectModules,
                healthy            : checkstyle.healthy,
                thresholdLimit     : checkstyle.thresholdLimit,
                unHealthy          : checkstyle.unHealthy
        ])
    }

    def "test setupBuild"() {
        setup:
        MavenBuild build = new MavenBuild()
        CheckstyleGoal checkstyle = new CheckstyleGoal()

        when:
        checkstyle.setupBuild(null, build)

        then:
        !build.goals.contains("checkstyle:checkstyle -Dcheckstyle.skip=false")
    }

}