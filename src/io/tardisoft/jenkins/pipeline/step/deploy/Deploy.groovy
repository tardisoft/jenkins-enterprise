package io.tardisoft.jenkins.pipeline.step.deploy

import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Steps that should be used to deploy a build
 */
interface Deploy extends Step {

    /**
     * Deploy build result as a SNAPSHOT
     * @param script Jenkinsfile script context
     */
    def deploySnapshot(def script)

    /**
     * Deploy build result as a RELEASE
     * @param script Jenkinsfile script context
     */
    def deployRelease(def script)

    /**
     * Compute the tag string to use
     * @param script Jenkinsfile script context
     */
    def getTagString(def script)
}
