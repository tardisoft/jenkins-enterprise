package io.tardisoft.jenkins.pipeline.step

/**
 * Setup the Properties of the Jenkins Job, like dats to keep artifacts, log history, etc.
 */
class PropertiesStep implements Serializable, Step {

    /**
     * Days to keep artifacts
     */
    String artifactDaysToKeepStr = ''
    /**
     * Absolute number of artifacts to keep
     */
    String artifactNumToKeepStr = '3'
    /**
     * Days to keep build information
     */
    String daysToKeepStr = ''
    /**
     * Absolute number of builds to keep
     */
    String numToKeepStr = '25'

    /**
     * List of job parameters to add top the job, example include choice, boolean etc.
     *
     * Example:
     * <code>
     *  properties.parameters=[
     choice(choices: 'MARATHON,COMPOSE', description: 'Target environment for integration tests', name: 'IT_ENV'),
     booleanParam(defaultValue: false, description: 'Check to enable execution of the secondary set of external service tests job', name: 'RUN_SECONDARY_EXTERNAL_SERVICE_TESTS'),
     ],
     * </code>
     */
    List<Object> parameters = []

    /**
     * Creates the config object for properties
     * @param script Jenkinsfile script context
     * @return Config
     */
    def createConfig(def script) {
        def config = [
                script.buildDiscarder(script.logRotator(
                        artifactDaysToKeepStr: artifactDaysToKeepStr,
                        artifactNumToKeepStr: artifactNumToKeepStr,
                        daysToKeepStr: daysToKeepStr,
                        numToKeepStr: numToKeepStr)
                ),
                script.disableConcurrentBuilds()
        ]
        if (parameters) {
            config.add(script.parameters(parameters))
        }
        return config
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        def config = createConfig(script)

        script.properties(config)
    }
}