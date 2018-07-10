package io.tardisoft.jenkins.pipeline.gradle.build

import io.tardisoft.jenkins.pipeline.build.Build
import io.tardisoft.jenkins.pipeline.gradle.build.goal.CheckstyleGoal
import io.tardisoft.jenkins.pipeline.gradle.build.goal.GradleBuildGoal
import io.tardisoft.jenkins.pipeline.gradle.build.goal.JUnitGoal
import io.tardisoft.jenkins.pipeline.gradle.step.GradleStep
import org.apache.commons.lang.StringUtils

/**
 * The base  Gradle build step.
 * This composes a set of build goals to produce a final build artifact.  You should do
 * compilation, testing, and basic static code analysis within this step.
 *
 * Integration tests should follow later in their own stage and step
 */
class GradleBuild extends GradleStep implements Serializable, Build {

    /**
     * List of gradle goals to call, default clean, install
     */
    LinkedHashSet<String> goals = ["clean", "test"]
    /**
     * True if gradle should enable the -fae option
     */
    boolean failAtEnd = true

    /**
     * List of gradle goals to execute and publish into Jenkins
     */
    List<GradleBuildGoal> gradleGoals = [
            new CheckstyleGoal(),
            new JUnitGoal()
    ]

    /**
     * Builds up a gradle command to invoke all the goals and executes it
     * Delegates to the actual goals to setup each of their goals
     * @param script , Jenkinsfile script context
     */
    @Override
    def run(def script) {
        for (GradleBuildGoal goal : gradleGoals) {
            goal.setupBuild(script, this)
        }
        List<String> args = []
        args.addAll(goals.findAll({ str -> StringUtils.isNotBlank(str) }))
        if (!failAtEnd) {
            args.add('--continue')
        }

        gradle script, args
    }

    /**
     * Publish the results of the various gradle plugins into Jenkins, such as Units test results, findbugs etc.
     * Delegates to the actual goals to publish each of their artifacts
     * @param script Jenkinsfile script context
     */
    @Override
    def publish(def script) {
        for (GradleBuildGoal goal : gradleGoals) {
            goal.run(script)
        }
    }
}
