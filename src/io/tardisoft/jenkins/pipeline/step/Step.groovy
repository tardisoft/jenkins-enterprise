package io.tardisoft.jenkins.pipeline.step

/**
 * An abstract Step of the Pipeline
 */
interface Step {

    /**
     * Run the step
     * @param script Jenkinsfile script context
     * @return Return value, anything
     */
    def run(def script)

}
