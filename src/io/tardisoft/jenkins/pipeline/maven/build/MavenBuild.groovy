package io.tardisoft.jenkins.pipeline.maven.build

import io.tardisoft.jenkins.pipeline.build.Build
import io.tardisoft.jenkins.pipeline.maven.build.goal.CheckstyleGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.FindBugsGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.JUnitGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.JacocoGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.JapiccGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.MavenBuildGoal
import io.tardisoft.jenkins.pipeline.maven.build.goal.SonarGoal
import io.tardisoft.jenkins.pipeline.maven.step.MavenStep
import org.apache.commons.lang.StringUtils

/**
 * The base  Maven build step.
 * This composes a set of build goals to produce a final build artifact.  You should do
 * compilation, testing, and basic static code analysis within this step.
 *
 * Integration tests should follow later in their own stage and step
 */
class MavenBuild extends MavenStep implements Serializable, Build {

    /**
     * Path to room to use, usually pom.xml
     */
    String rootPom = 'pom.xml'
    /**
     * List of maven goals to call, default clean, install
     */
    LinkedHashSet<String> goals = ["clean", "install"]
    /**
     * True if maven should enable the -fae option
     */
    boolean failAtEnd = true

    /**
     * List of maven goals to execute and publish into Jenkins
     */
    List<MavenBuildGoal> mavenGoals = [
            new FindBugsGoal(),
            new CheckstyleGoal(),
            new JUnitGoal(),
            new JapiccGoal(),
            new SonarGoal(),
            new JacocoGoal()
    ]

    /**
     * Builds up a maven command to invoke all the goals and executes it
     * Delegates to the actual goals to setup each of their goals
     * @param script , Jenkinsfile script context
     */
    @Override
    def run(def script) {
        for (MavenBuildGoal goal : mavenGoals) {
            goal.setupBuild(script, this)
        }
        List<String> args = []
        args.add("-U")
        if (StringUtils.isNotBlank(rootPom)) {
            args.add("-f $rootPom")
        }
        args.addAll(goals.findAll({ str -> StringUtils.isNotBlank(str) }))
        if (failAtEnd) {
            args.add('-fae')
        }

        mvn script, args
    }

    /**
     * Publish the results of the various maven plugins into Jenkins, such as Units test results, findbugs etc.
     * Delegates to the actual goals to publish each of their artifacts
     * @param script Jenkinsfile script context
     */
    @Override
    def publish(def script) {
        for (MavenBuildGoal goal : mavenGoals) {
            goal.run(script)
        }
    }
}
