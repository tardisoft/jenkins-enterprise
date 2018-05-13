package io.tardisoft.jenkins.pipeline.build

import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Base type to represent a build
 */
interface Build extends Step {

    /**
     * Build publish phase
     * @param script Jenkinsfile script context
     */
    def publish(def script)

    /**
     * Build phase
     * @param script Jenkinsfile script context
     */
    def run(def script)
}
