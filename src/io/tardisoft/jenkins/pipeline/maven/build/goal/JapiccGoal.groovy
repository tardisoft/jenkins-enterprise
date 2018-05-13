package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the Java API Compliance Checker (JAPICC) build goal. https://lvc.github.io/japi-compliance-checker/
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 */
class JapiccGoal implements Serializable, MavenBuildGoal {

    @Override
    def run(def script) {

    }

    @Override
    void setupBuild(def script, MavenBuild build) {

    }

    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.add("-Djapicc.skip=true")
    }
}