package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the Jacoco build goal and publish the results of the build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="http://www.eclemma.org/jacoco/trunk/doc/maven.html">Maven Jacoco Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/JaCoCo+Plugin">Jenkins Jacoco Plugin</a>
 */
class JacocoGoal implements Serializable, MavenBuildGoal {

    @Override
    void setupBuild(def script, MavenBuild build) {

    }

    @Override
    void setupDeploy(Object script, MavenStep build) {

    }

    def createConfig() {
        return ['$class': 'JacocoPublisher']
    }

    @Override
    def run(def script) {
        def config = createConfig()
        config['$class'] = 'JacocoPublisher'
        script.step(config)
    }
}