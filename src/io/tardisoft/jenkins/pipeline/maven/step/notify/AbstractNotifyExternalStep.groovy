package io.tardisoft.jenkins.pipeline.maven.step.notify

import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Step to deploy maven artifacts to artifactory
 */
abstract class AbstractNotifyExternalStep extends MavenStep implements Serializable, Step {
    /**
     * {@inheritDoc}
     */
    abstract run(def script)

    /**
     * Update the associated Github pull request
     * @param script Jenkinsfile script context
     */

    abstract notifyExternal(def script)
}
