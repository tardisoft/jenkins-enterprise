package io.tardisoft.jenkins.pipeline.step

import io.tardisoft.jenkins.pipeline.util.Common

/**
 * Checkout the code from the git repository
 */
class CheckoutStep implements Serializable, Step {
    /**
     * Git commiter name to use
     */
    String GIT_COMMITTER_NAME = "Jenkins"
    /**
     * Git commiter email to use
     */
    String GIT_COMMITTER_EMAIL = "defaultuser@nowhere.com"
    /**
     * Clean step before cloning
     */
    CleanStep cleanStep = new CleanStep()

    /**
     * Create the config object for jenkins
     * @param script Jenkinsfile script context
     * @return Config object
     */
    def createConfig(def script) {
        String branchName = new Common(script).getBranch()
        def branches = script.scm.branches ?: []
        List extensions = []
        List oldExtensions = script.scm.extensions
        if (oldExtensions) {
            extensions.addAll(oldExtensions)
        }
        extensions.addAll([[$class: 'CloneOption', noTags: false], [$class: 'CleanCheckout'], [$class: 'LocalBranch', localBranch: "${branchName}"]])
        def userRemoteConfig = script.scm.userRemoteConfigs ?: []
        return [
                $class           : 'GitSCM',
                branches         : branches,
                extensions       : extensions,
                userRemoteConfigs: userRemoteConfig
        ]
    }

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        cleanStep.run(script)
        script.checkout(createConfig(script))
        script.sh "git config user.name ${GIT_COMMITTER_NAME}"
        script.sh "git config user.email ${GIT_COMMITTER_EMAIL}"
    }
}