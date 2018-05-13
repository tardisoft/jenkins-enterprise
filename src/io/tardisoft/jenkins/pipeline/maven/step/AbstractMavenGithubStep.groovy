package io.tardisoft.jenkins.pipeline.maven.step

import io.tardisoft.jenkins.pipeline.maven.build.goal.MavenBuildGoal
import io.tardisoft.jenkins.pipeline.util.Common


abstract class AbstractMavenGithubStep extends MavenStep implements Serializable {
    /**
     * List of release branches
     */
    List<String> releaseBranches = []
    /**
     * Jenkins secrets ID for git credentials
     */
    String gitCredentialsId = 'github'
    /**
     * True if should deploy snapshots
     */
    boolean deploySnapshots = false
    /**
     * Maven goals to disable
     */
    Set<MavenBuildGoal> mavenGoals = []

    /**
     * Flag indicating if build should fail if docs fail
     */
    boolean shouldFailBuild = false

    /**
     * maven profiles to use
     */
    Set<String> mavenProfiles = []
    Set<String> mavenTargets = []

    String version

    /**
     * {@inheritDoc}
     */
    @Override
    def run(def script) {
        for (MavenBuildGoal g : mavenGoals) {
            g.setupDeploy(script, this)
        }

        boolean isReleaseBranch = new Common(script).isReleaseBranch(releaseBranches)
        if (isReleaseBranch) {
            deployRelease(script)
        } else {
            deploySnapshot(script)
        }
    }

    /**
     * Deploy snapshot Git Hub page
     */
    def deploySnapshot(def script) {
        if (deploySnapshots) {
            deployRelease(script)
        }
    }

    /**
     * Deploy release github page
     * @param script Jenkinsfile script context
     */
    def deployRelease(def script) {
        script.echo "Empty deploy, no implementation provided"
    }
}
