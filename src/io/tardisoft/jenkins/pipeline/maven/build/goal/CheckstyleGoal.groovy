package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the checkstyle build goal and publish the results of the checkstyle build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="https://maven.apache.org/plugins/maven-checkstyle-plugin">Maven Checkstyle Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/Checkstyle+Plugin">Jenkins Checkstyle Plugin</a>
 */
class CheckstyleGoal implements Serializable, MavenBuildGoal {
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    boolean canComputeNew = false
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    String defaultEncoding = 'UTF-8'
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    String pattern = '**/checkstyle-result.xml'
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    boolean shouldDetectModules = true
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    String healthy = ''
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    String thresholdLimit = 'high'
    /**
     * Checkstyle plugin option see plugin documentation for details
     */
    String unHealthy = ''

    /**
     * Create the configuration block for jenkins
     * @return config block
     */
    def createConfig() {
        return [
                $class             : 'CheckStylePublisher',
                canComputeNew      : canComputeNew,
                defaultEncoding    : defaultEncoding,
                pattern            : pattern,
                shouldDetectModules: shouldDetectModules,
                healthy            : healthy,
                thresholdLimit     : thresholdLimit,
                unHealthy          : unHealthy
        ]
    }

    /**
     * Setup the publisher to display the checkstyle results
     * @param script Jenkinsfile script context
     */
    @Override
    def run(def script) {
        def config = createConfig()
        config['$class'] = 'CheckStylePublisher'
        script.step(config)
    }

    /**
     * Called by the build to setup checkstyle for the build
     * @param script Jenkinsfile script context
     * @param build Build to modify
     */
    @Override
    void setupBuild(def script, MavenBuild build) {

    }

    /**
     * Disables checkstyle during the deploy phases
     * @param script Jenkinsfile script context
     * @param build Maven step to disable checkstyle in
     */
    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.add("-Dcheckstyle.skip=true")
    }
}