package io.tardisoft.jenkins.pipeline.gradle.step

import io.tardisoft.jenkins.pipeline.release.BasicReleaseStrategy
import io.tardisoft.jenkins.pipeline.release.ReleaseStrategy
import io.tardisoft.jenkins.pipeline.step.Step
import io.tardisoft.jenkins.pipeline.util.Common

/**
 * Called at the beginning of the build to "update" the pom.version
 *
 * This is used for changing the version to a release version, such as 1.1.0-SNAPSHOT changes to 1.1.0 or
 * to update the version to something specific to the branch, such as 1.1.0-SNAPSHOT changes to 1.1.0.BRANCH-SNAPSHOT
 *
 * How the version is actually calculated is delegated to the {@link ReleaseStrategy}
 *
 */
class GradleUpdateVersionStep extends GradleStep implements Serializable, Step {

    /**
     * List of release branches
     */
    List<String> releaseBranches = []
    /**
     * Release or Update strategy to use, defaults to {@link io.tardisoft.jenkins.pipeline.release.BasicReleaseStrategy}
     */
    ReleaseStrategy updateStrategy = new BasicReleaseStrategy()
    /**
     * The new version that was calculated
     */
    String newVersion

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        def pom = [version: '1.0.0']
        boolean isReleaseBranch = new Common(script).isReleaseBranch(releaseBranches)
        script.echo "Release Branch: ${isReleaseBranch}"
        script.echo "Using strategy ${updateStrategy.class.simpleName}"

        if (isReleaseBranch) {
            newVersion = updateStrategy.releaseVersion(script, pom)
            script.currentBuild.displayName = newVersion
        } else {
            newVersion = updateStrategy.snapshotVersion(script, pom)
        }

        if (newVersion) {
            script.echo "Building Version: ${newVersion}"
            List<String> args = []
            args.add("-Pversion=${newVersion}")
            gradle(script, args)
        } else {
            script.echo "Update version skipped"
        }
    }
}
