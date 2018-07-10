package io.tardisoft.jenkins.pipeline.gradle.build.goal

import io.tardisoft.jenkins.pipeline.gradle.build.GradleBuild
import io.tardisoft.jenkins.pipeline.gradle.step.GradleStep

/**
 * Setup the JUnit build goal and publish the results of the build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="http://maven.apache.org/surefire/maven-surefire-plugin/">Maven Surefire Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/JUnit+Plugin">Jenkins JUnit Plugin</a>
 */
class JUnitGoal implements Serializable, GradleBuildGoal {
    def testDataPublishers = [[$class: 'AttachmentPublisher']]
    def testResults = '**/build/test-results/**/*.xml'
    def allowEmptyResult = true
    /**
     * Set to true to not fail on build on test failure.  This is useful if you would like the junit publisher to
     * instead mark the build unstable on failed tests
     */
    boolean testFailsureIgnore = false

    def createConfig() {
        return [
                testDataPublishers: testDataPublishers,
                testResults       : testResults,
                allowEmptyResults : allowEmptyResult
        ]
    }

    @Override
    def run(def script) {
        def config = createConfig()
        script.junit(config)
    }

    @Override
    void setupBuild(def script, GradleBuild build) {
        build.goals += 'check'
        if (testFailsureIgnore) {
            build.gradleArgs.add("-x test ")
        }
    }

    @Override
    void setupDeploy(Object script, GradleStep build) {
        build.gradleArgs.addAll([
                "-x test "
        ])
    }
}