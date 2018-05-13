package io.tardisoft.jenkins.pipeline.maven.step

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class MavenStepSpec extends Specification {

    MavenStep createStep() {
        return new MavenStep() {
            @Override
            def run(Object script) {
                return null
            }
        }
    }

    def "test getMavenOpts"() {
        setup:
        MavenStep step = createStep()

        when:
        def result = step.getMavenOpts()

        then:
        result == MavenStep.DEFAULT_JVM_ARGS.join(" ")

        when:
        step.jvmArgs = ['-Xmx2g']
        result = step.getMavenOpts()

        then:
        result == "-Xmx2g"
    }

    def "test getMavenArgLine"() {
        setup:
        MavenStep step = createStep()
        step.localRepo = false

        when:
        def result = step.getMavenArgLine()

        then:
        result == MavenStep.DEFAULT_MAVEN_ARGS.join(" ")

        when:
        step.mavenArgs = ['-U']
        result = step.getMavenArgLine()

        then:
        result == '-U'
    }

    def "test mvn"() {
        setup:
        MavenBuild step = new MavenBuild()
        step.localRepo = true
        JenkinsScript jenkins = Spy(JenkinsScript)
        step.jvmArgs = ['-Xmx2g']
        step.mavenArgs = ['-b']

        when:
        jenkins.files = [:]
        step.mvn(jenkins, "help")

        then:
        1 * jenkins.fileExists('mvnw')
        1 * jenkins.sh(
                'export MAVEN_OPTS="-Xmx2g" && mvn -b -Dmaven.repo.local=$PWD/.repository help'
        )

        when:
        jenkins.files = ['mvnw': 'data']
        step.localRepo = false
        step.mvn(jenkins, "help")

        then:
        1 * jenkins.fileExists('mvnw')
        1 * jenkins.sh(
                'export MAVEN_OPTS="-Xmx2g" && ./mvnw -b help'
        )
    }
}
