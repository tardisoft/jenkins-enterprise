package io.tardisoft.jenkins.pipeline.gradle

import io.tardisoft.jenkins.pipeline.AbstractJenkinsPipeline
import io.tardisoft.jenkins.pipeline.gradle.build.GradleBuild
import io.tardisoft.jenkins.pipeline.gradle.step.GradleStep
import io.tardisoft.jenkins.pipeline.notify.AbstractNotifyExternalStep
import io.tardisoft.jenkins.pipeline.step.ArchiveArtifactsStep
import io.tardisoft.jenkins.pipeline.step.Step

/**
 * Builds a gradle based project
 * 1. Checks out code
 * 2. Updates the version to be either a release, or a branch build
 * 3. Run the build, tests, and static code analysis
 * 4. Publish the build results to Jenkins
 * 5. Run through additional Quality gate (aka Sonar)
 * 6. Tag and deploy the artifacts to artifactory
 * 7. Any final cleanup
 */
class GradleJenkinsPipeline extends AbstractJenkinsPipeline implements Serializable {

    /**
     * Any Java Virtual Machine arguments to add to ALL the gradle invocations
     */
    List<String> jvmArgs = []
    /**
     * Any additional gradle arguments to add to ALL gradle invocations
     */
    List<String> gradleArgs = []
    /**
     * Will deploy and download all artifacts to a gradle repository relative to the local build
     */
    boolean localRepo = true

    /**
     * Responsible for invoking the build phase
     */
    GradleBuild gradleBuildStep = new GradleBuild()

    /**
     * List of steps to include in the publisher stage
     */
    List<Step> publishers = [
            new ArchiveArtifactsStep()
    ]

    /**
     * Runs quality metrics (aka Sonar)
     */
    List<AbstractNotifyExternalStep> notifyExternalStep = []

    /**
     * {@inheritDoc}
     */
    @Override
    void buildAndTest(def script) {
        buildGradle(script)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void qualityGate(def script) {

    }

    @Override
    void notifyExternal(Object script) {
        notifyExternalStep?.each {
            it.run(script)
        }
    }

    /**
     * Root of the gradle build process, includes setup steps and invoking the build
     * @param script Jenkinsfile script context
     */
    void buildGradle(def script) {
        buildGradleStep(script)
    }

    /**
     * Actually invoke the gradle build
     * @param script Jenkinsfile script context
     */
    void buildGradleStep(def script) {
        if (gradleBuildStep == null) {
            return
        }

        addGradleConfig(gradleBuildStep)

        gradleBuildStep.run(script)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void publish(def script) {
        gradleBuildStep?.publish(script)
        for (Step step : publishers) {
            try {
                step.run(script)
            } catch (Exception e) {
                script.echo "Error: ${e?.getMessage()}, but continuing"
            }
        }
    }

    /**
     * Update the gradle command with shared gradle configuration options, such as jvm args, gradle args etc.
     * @param step Jenkinsfile script context
     */
    void addGradleConfig(GradleStep step) {
        if (jvmArgs) {
            LinkedHashSet<String> mergeArgs = new LinkedHashSet<String>()
            mergeArgs.addAll(jvmArgs)
            mergeArgs.addAll(step.jvmArgs)
            step.jvmArgs = mergeArgs as List<String>
        }
        if (gradleArgs) {
            LinkedHashSet<String> mergeArgs = new LinkedHashSet<String>()
            mergeArgs.addAll(gradleArgs)
            mergeArgs.addAll(step.gradleArgs)
            step.gradleArgs = mergeArgs as List<String>
        }
        step.localRepo = localRepo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deploy(def script) {

    }

    @Override
    void site(def script) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    void cleanup(def script) {

    }
}
