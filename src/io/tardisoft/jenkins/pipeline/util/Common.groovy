package io.tardisoft.jenkins.pipeline.util

import com.cloudbees.groovy.cps.NonCPS
import org.apache.commons.lang.StringUtils

/**
 * Utility class of command functions
 */
class Common implements Serializable {

    /**
     * Jenkinsfile script context
     */
    def script

    /**
     * Constructor
     * @param script Jenkinsfile script context to use for all calls
     */
    Common(def script) {
        this.script = script
    }

    /**
     * True is the current branch is within the provided list of release branches, OR matches on of the patterns in the list
     * @param releaseBranches list of releases and/or patterns
     * @return True is is considered a release
     */
    boolean isReleaseBranch(List<String> releaseBranches) {
        String currentBranch = getBranch()
        for (String s : releaseBranches) {
            if (currentBranch?.matches(s)) {
                return true
            }
        }
        return false
    }

    /**
     * Get the current branch
     * @return name of current branch
     */
    String getBranch() {
        String retVal = script.env.BRANCH_NAME
        if (StringUtils.isBlank(retVal)) {
            try {
                retVal = script.sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true)
                if (retVal == "HEAD") {
                    retVal = script.sh(script: 'git rev-parse HEAD', returnStdout: true)
                }
            } catch (Exception e) {
                script.echo "Failed to determine branch: ${e}"
            }
        }
        return retVal
    }

    /**
     * If the current branch is considered to be a pull-request
     * @return True if is a pull request
     */
    boolean isPullRequest() {
        return getBranch()?.matches("PR-.+")
    }

    @NonCPS
    String getOrgRepo() {
        String url = script.scm?.userRemoteConfigs?.first()?.url ?: ''
        List o = (url =~ /^https:\/\/(.*)\/(.*)\/(.*)\.git$/)?.collect { m, h, o, r -> [o, r] }
        o ? o.first().join('/') : ''
    }

    @NonCPS
    String getRepo() {
        String url = script.scm?.userRemoteConfigs?.first()?.url ?: ''
        List r = (url =~ /^https:\/\/(.*)\/(.*)\/(.*)\.git$/)?.collect { m, h, o, r -> r }
        r ? r.first() : ''
    }

    @NonCPS
    String getOrg() {
        String url = script.scm?.userRemoteConfigs?.first()?.url ?: ''
        List o = (url =~ /^https:\/\/(.*)\/(.*)\/(.*)\.git$/)?.collect { m, h, o, r -> o }
        o ? o.first() : ''
    }
}