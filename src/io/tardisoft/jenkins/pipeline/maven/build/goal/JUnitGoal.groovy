package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

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
class JUnitGoal implements Serializable, MavenBuildGoal {
    def testDataPublishers = [[$class: 'AttachmentPublisher']]
    def testResults = '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
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
    void setupBuild(def script, MavenBuild build) {
        build.goals += 'verify'
        if (testFailsureIgnore) {
            build.mavenArgs.add("-Dmaven.test.failure.ignore=true")
        }
    }

    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.addAll([
                "-DskipTests=true",
                "-Dmaven.test.skip=true",
        ])
    }
}