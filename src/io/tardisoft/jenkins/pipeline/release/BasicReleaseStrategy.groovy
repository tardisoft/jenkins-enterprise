package io.tardisoft.jenkins.pipeline.release

import com.cloudbees.groovy.cps.NonCPS
import io.tardisoft.jenkins.pipeline.util.Common
import org.apache.commons.lang.StringUtils

/**
 * Basic release versioning strategy that strips snapshot name for releases and adds branch name for branch snapshot builds
 */
class BasicReleaseStrategy implements ReleaseStrategy, Serializable {
    final String replaceSuffix
    final String replacementPrefix

    /**
     * @param replaceSuffix suffix to replace
     * @param replacementPrefix replacement prefix
     */
    BasicReleaseStrategy(String replaceSuffix = '-SNAPSHOT', String replacementPrefix = '.') {
        this.replacementPrefix = replacementPrefix
        this.replaceSuffix = replaceSuffix
    }

    /**
     * Adds the branch name to the version of the build
     * @param script Jenkinsfile script context
     * @param pom Parent pom to update versions in
     * @returnNew New version to use
     */
    @Override
    String snapshotVersion(def script, def pom) {
        final String sourceBranch = new Common(script).getBranch()
        final String branchName = updateBranchName(sourceBranch)
        final String suffix
        if (StringUtils.isNotBlank(branchName)) {
            if (branchName.endsWith(replaceSuffix)) {
                suffix = branchName
            } else {
                suffix = branchName + replaceSuffix
            }
        } else {
            suffix = replaceSuffix
        }
        return pom?.version?.replace(replaceSuffix, "${replacementPrefix}${suffix}")
    }

    /**
     * Replaces the suffix with the prefix and get release suffix
     */
    @NonCPS
    @Override
    String releaseVersion(def script, def pom) {
        return pom?.version?.replace(replaceSuffix, "${replacementPrefix}${getReleaseSuffix(script)}")
    }

    /**
     * Suffix to use for releases, default Jenkins build number
     * @param script Jenkinsfile script to use
     * @return new suffix
     */
    String getReleaseSuffix(def script) {
        return script.currentBuild.number
    }

    /**
     * Removes specials characters from the branch name that can't be in a version
     * @param branchName Branch name to 'fix'
     * @return version safe branch name
     */
    String updateBranchName(String branchName) {
        return branchName?.replaceAll('[\\\\/:"<>\\|\\?\\*]', '_')
    }
}
