package io.tardisoft.jenkins.pipeline.step

/**
 * Archives build artifacts into Jenkins
 */
class ArchiveArtifactsStep implements Serializable, Step {

    /**
     * True if should NOT error when no artifacts were found
     */
    boolean allowEmptyArchive = true
    /**
     * Comma separated list of ant file path patterns to be archived
     */
    String artifacts = '**/target/usage*.txt,**/target/japicc-compat-reports/compat_report.html,**/build/usage*.txt,**/build/japicc-compat-reports/compat_report.html'
    /**
     * Comma separated list of ant file path patterns to be excluded,  this is applied after the includes pattern.
     */
    String excludes = ''
    /**
     * Only archive artifacts if the build is successful
     */
    boolean onlyIfSuccessful = true

    /**
     * Create the configuration object for the archive step
     * @param script Jenkinsfile script context
     */
    def createConfig(def script) {
        def config = [
                allowEmptyArchive: allowEmptyArchive,
                artifacts        : artifacts,
                excludes         : excludes,
                onlyIfSuccessful : onlyIfSuccessful
        ]
        return config
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        def config = createConfig(script)
        script.archiveArtifacts(config)
        return null
    }
}
