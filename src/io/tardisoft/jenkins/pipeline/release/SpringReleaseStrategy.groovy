package io.tardisoft.jenkins.pipeline.release

import com.cloudbees.groovy.cps.NonCPS

/**
 * A release strategy based on the spring version naming conventions
 */
class SpringReleaseStrategy extends GitTagReleaseStrategy implements ReleaseStrategy, Serializable {

    /**
     * {@inheritDoc}
     */
    @Override
    @NonCPS
    String releaseVersion(def script, def pom) {
        script.echo "DEBUG: SpringReleaseStrategy without super"
        return releaseVersionCalculate(script, pom, 'RELEASE', '.')
    }
}
