package io.tardisoft.jenkins.pipeline.gradle.build.goal

import io.tardisoft.jenkins.pipeline.gradle.build.GradleBuild
import io.tardisoft.jenkins.pipeline.gradle.step.GradleStep

/**
 * Setup the checkstyle build goal and publish the results of the checkstyle build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Gradle tiles
 *
 * @see <a href="https://gradle.apache.org/plugins/gradle-checkstyle-plugin">Gradle Checkstyle Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/Checkstyle+Plugin">Jenkins Checkstyle Plugin</a>
 */
class CheckstyleGoal implements Serializable, GradleBuildGoal {
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
    String pattern = '**/build/reports/checkstyle/main.xml'
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
    void setupBuild(def script, GradleBuild build) {
        build.goals += "checkstyleMain"
    }

    /**
     * Disables checkstyle during the deploy phases
     * @param script Jenkinsfile script context
     * @param build Gradle step to disable checkstyle in
     */
    @Override
    void setupDeploy(Object script, GradleStep build) {
        build.gradleArgs.addAll([
                "-x checkstyleMain "
        ])
    }
}