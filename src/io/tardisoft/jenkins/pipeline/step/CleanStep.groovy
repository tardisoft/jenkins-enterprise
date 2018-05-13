package io.tardisoft.jenkins.pipeline.step

/**
 * Wipes the workspace
 */
class CleanStep implements Serializable, Step {

    /**
     * True if should not fail the build if the clean runs into problems
     */
    boolean notFailBuild = true

    /**
     * Creat the jenkins config object
     * @param script Jenkinsfile script context
     * @return config object
     */
    def createConfig(def script) {
        return [
                '$class'      : 'WsCleanup',
                'notFailBuild': notFailBuild
        ]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        def config = createConfig(script)
        config['$class'] = 'WsCleanup'
        script.step(config)
    }
}