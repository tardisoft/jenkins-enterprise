package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.build.MavenBuild
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep

/**
 * Setup the Cobertura build goal and publish the results of the build to Jenkins.
 *
 * Setup of the actual pom is the responsibility of the individual repository developer.
 *
 * To simplify setup consider using Composable Maven tiles
 *
 * @see <a href="http://www.mojohaus.org/cobertura-maven-plugin/">Maven Cobertura Plugin</a>
 * @see <a href="https://wiki.jenkins.io/display/JENKINS/Cobertura+Plugin">Jenkins Cobertura Plugin</a>
 */
class CoberturaGoal implements Serializable, MavenBuildGoal {

    /**
     * {@inheritDoc }
     */
    @Override
    void setupBuild(def script, MavenBuild build) {

    }

    /**
     * {@inheritDoc }
     */
    @Override
    void setupDeploy(Object script, MavenStep build) {
        build.mavenArgs.add('-Dcobertura.skip=true')
    }

    /**
     * Create the Jenkins config block for the publisher
     * @return jenkins config block
     */
    def createConfig() {
        return [$class             : 'CoberturaPublisher',
                autoUpdateHealth   : false,
                autoUpdateStability: false,
                coberturaReportFile: ' **/target/site/cobertura/coverage.xml',
                failUnhealthy      : false,
                failUnstable       : false,
                maxNumberOfBuilds  : 0,
                onlyStable         : false,
                sourceEncoding     : 'UTF_8',
                zoomCoverageChart  : false]
    }

    /**
     * Add the publisher to jenkins job
     * @param script Jenkinfile script context
     */
    @Override
    def run(Object script) {
        def config = createConfig()
        config['$class'] = 'CoberturaPublisher'
        script.step(config)
    }
}
