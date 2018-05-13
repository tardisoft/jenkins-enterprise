package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the Sonar build goal and publish the results of the build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven">Maven SonarQube Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/SonarQube+plugin">Jenkins SonarQube Plugin</a>
 */
class SonarGoal implements Serializable, MavenBuildGoal {

    @Override
    def run(def script) {

    }

    @Override
    void setupBuild(def script, MavenBuild build) {

    }

    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.add("-Dsonar.skip=true")
    }
}