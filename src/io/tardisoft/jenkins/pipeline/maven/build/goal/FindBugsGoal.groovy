package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the Findbugs build goal and publish the results of the build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="https://gleclaire.github.io/findbugs-maven-plugin/">Maven Findbugs Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/FindBugs+Plugin">Jenkins Findbugs Plugin</a>
 */
class FindBugsGoal implements Serializable, MavenBuildGoal {
    boolean canComputeNew = false
    String defaultEncoding = 'UTF-8'
    String pattern = '**/findbugsXml.xml'
    boolean shouldDetectModules = true
    String excludePattern = ''
    String failedTotalAll = '0'
    String healthy = ''
    String includePattern = ''
    String unHealthy = ''
    String thresholdLimit = 'high'

    /**
     * Create the Findbugs publisher configuration block
     * @return
     */
    def createConfig() {
        def config = [
                $class             : 'FindBugsPublisher',
                canComputeNew      : canComputeNew,
                defaultEncoding    : defaultEncoding,
                excludePattern     : excludePattern,
                failedTotalAll     : failedTotalAll,
                healthy            : healthy,
                includePattern     : includePattern,
                pattern            : pattern,
                shouldDetectModules: shouldDetectModules,
                unHealthy          : unHealthy,
                thresholdLimit     : thresholdLimit
        ]
        return config
    }

    /**
     * Add the findbugs publisher to the build
     * @param script Jenkinsfile script context
     */
    @Override
    def run(def script) {
        def config = createConfig()
        config['$class'] = 'FindBugsPublisher'
        script.step(config)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setupBuild(def script, MavenBuild build) {
    }

    /**
     * Disables the findbugs maven plugin
     * @param script Jenkinsfile script context
     * @param build build to modify
     */
    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.add("-Dfindbugs.skip=true")
    }
}