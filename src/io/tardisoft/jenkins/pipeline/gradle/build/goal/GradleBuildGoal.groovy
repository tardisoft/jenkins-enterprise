package io.tardisoft.jenkins.pipeline.gradle.build.goal

import io.tardisoft.jenkins.pipeline.gradle.build.GradleBuild
import io.tardisoft.jenkins.pipeline.gradle.step.GradleStep
import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Setup a gradle build goal
 */
interface GradleBuildGoal extends Step {
    /**
     * Adds any args to the passed build to invoke this goal during the build.
     * Typically this method does nothing as we recommend MOST plgins be enabled within the pom
     * to ensure local builds behave the same way as build on Jenkins
     * @param script Jenkinsfile script context
     * @param build builds to modify
     */
    void setupBuild(def script, GradleBuild build)

    /**
     * Modify the passed in gradle step for the correct behavior during a deploy. Typically this
     * means adding 'skip' properties to disable a plugin since most plugins shouldn't be executed
     * again during the deploy
     * @param script Jenkinsfile script context
     * @param build build to modify
     */
    void setupDeploy(def script, GradleStep build)
}