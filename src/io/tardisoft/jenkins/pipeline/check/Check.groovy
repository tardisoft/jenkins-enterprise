package io.tardisoft.jenkins.pipeline.check

import jenkins.model.Jenkins

/**
 * A Check to run before the build
 */
interface Check {
    /**
     * Validate the Jenkins instance the build is attempting to run in
     * @param script Jenkinsfile script context
     * @param jenkins Jenkins instance to check
     * @return True if build should continue
     */
    boolean run(def script, Jenkins jenkins)
}
