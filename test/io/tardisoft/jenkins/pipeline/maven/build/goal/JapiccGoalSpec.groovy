package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.JapiccGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class JapiccGoalSpec extends Specification {

    def "test setupBuild"() {
        setup:
        MavenBuild build = new MavenBuild()
        JapiccGoal sonarGoal = new JapiccGoal()

        when:
        sonarGoal.setupBuild(null, build)

        then:
        !build.mavenArgs.contains("-Djapicc.skip=true")
    }

    def "test setupDeploy"() {
        setup:
        MavenBuild build = new MavenBuild()
        JapiccGoal sonarGoal = new JapiccGoal()

        when:
        sonarGoal.setupDeploy(null, build)

        then:
        build.mavenArgs.contains("-Djapicc.skip=true")
    }

    def "test run"() {
        given:
        JenkinsScript jenkinsScript = new JenkinsScript()

        when:
        new JapiccGoal().run(jenkinsScript)

        then:
        notThrown(Exception)
    }

    def "test run null script"() {
        when:
        new JapiccGoal().run(null)

        then:
        notThrown(Exception)
    }

    def "test run no args"() {
        when:
        new JapiccGoal().run()

        then:
        notThrown(Exception)
    }

}