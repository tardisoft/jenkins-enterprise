package io.tardisoft.jenkins.pipeline.notify

import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Step to notify external
 */
abstract class AbstractNotifyExternalStep implements Serializable, Step {
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
