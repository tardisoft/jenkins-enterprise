package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.SonarGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class SonarGoalSpec extends Specification {

    def "test setupBuild"() {
        setup:
        MavenBuild build = new MavenBuild()
        SonarGoal sonarGoal = new SonarGoal()

        when:
        sonarGoal.setupBuild(null, build)

        then:
        !build.mavenArgs.contains("-Dsonar.skip=true")
    }

    def "test setupDeploy"() {
        setup:
        MavenBuild build = new MavenBuild()
        SonarGoal sonarGoal = new SonarGoal()

        when:
        sonarGoal.setupDeploy(null, build)

        then:
        build.mavenArgs.contains("-Dsonar.skip=true")
    }

    def "test run"() {
        given:
        JenkinsScript jenkinsScript = new JenkinsScript()

        when:
        new SonarGoal().run(jenkinsScript)

        then:
        notThrown(Exception)
    }

    def "test run null script"() {
        when:
        new SonarGoal().run(null)

        then:
        notThrown(Exception)
    }

    def "test run no args"() {
        when:
        new SonarGoal().run()

        then:
        notThrown(Exception)
    }

}