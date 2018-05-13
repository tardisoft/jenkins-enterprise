package io.tardisoft.jenkins.pipeline.maven.build

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.build.goal.MavenBuildGoal
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class MavenBuildSpec extends Specification {

    def "test run"() {
        setup:
        JenkinsScript jenkins = Spy(JenkinsScript)
        MavenBuild step = new MavenBuild()
        step.failAtEnd = true
        step.rootPom = 'parent/pom.xml'
        step.localRepo = false
        step.jvmArgs = ['-Xmx2g']
        step.mavenArgs = ['-b']
        MavenBuildGoal goal1 = Mock(MavenBuildGoal)
        goal1.setupBuild(jenkins, step) >> {
            step.goals += "goal1"
        }
        MavenBuildGoal goal2 = Mock(MavenBuildGoal)
        goal2.setupBuild(jenkins, step) >> {
            step.goals += "goal2"
        }
        step.mavenGoals = [
                goal1,
                goal2
        ]
        String shArg = ''

        when:
        step.run(jenkins)

        then:
        1 * jenkins.sh(
                { arg -> shArg = arg }
        )

        and:
        shArg == 'export MAVEN_OPTS="-Xmx2g" && mvn -b -U -f parent/pom.xml clean install goal1 goal2 -fae'
    }

    def "test publish"() {
        setup:
        MavenBuild step = new MavenBuild()
        MavenBuildGoal goal = Mock(MavenBuildGoal)
        MavenBuildGoal goal2 = Mock(MavenBuildGoal)
        JenkinsScript jenkins = Spy(JenkinsScript)
        step.mavenGoals = [goal, goal2]

        when:
        step.publish(jenkins)

        then:
        1 * goal.run(jenkins)
    }

    def "test customize"() {
        setup:
        JenkinsScript jenkins = Spy(JenkinsScript)
        MavenBuild step = new MavenBuild()
        step.failAtEnd = true
        step.rootPom = 'parent/pom.xml'
        step.localRepo = false
        step.jvmArgs = ['-Xmx2g']
        step.mavenArgs = ['-b']
        step.mavenArgs.add("-T 2C")
        step.mavenArgs.add("-Pjacoco")
        MavenBuildGoal goal1 = Mock(MavenBuildGoal)
        goal1.setupBuild(jenkins, step) >> {
            step.goals += "goal1"
        }
        MavenBuildGoal goal2 = Mock(MavenBuildGoal)
        goal2.setupBuild(jenkins, step) >> {
            step.goals += "goal2"
        }
        step.mavenGoals = [
                goal1,
                goal2
        ]
        String shArg = ''

        when:
        step.run(jenkins)

        then:
        1 * jenkins.sh({ arg -> shArg = arg }

        )

        and:
        shArg == 'export MAVEN_OPTS="-Xmx2g" && mvn -b -T 2C -Pjacoco -U -f parent/pom.xml clean install goal1 goal2 -fae'
    }
}
