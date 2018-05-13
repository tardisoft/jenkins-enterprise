package io.tardisoft.jenkins.pipeline.maven.build.goal

import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Setup a maven build goal
 */
interface MavenBuildGoal extends Step {
    /**
     * Adds any args to the passed build to invoke this goal during the build.
     * Typically this method does nothing as we recommend MOST plgins be enabled within the pom
     * to ensure local builds behave the same way as build on Jenkins
     * @param script Jenkinsfile script context
     * @param build builds to modify
     */
    void setupBuild(def script, io.tardisoft.jenkins.pipeline.maven.build.MavenBuild build)

    /**
     * Modify the passed in maven step for the correct behavior during a deploy. Typically this
     * means adding 'skip' properties to disable a plugin since most plugins shouldn't be executed
     * again during the deploy
     * @param script Jenkinsfile script context
     * @param build build to modify
     */
    void setupDeploy(def script, MavenStep build)
}