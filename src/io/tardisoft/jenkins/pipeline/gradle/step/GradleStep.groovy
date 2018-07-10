package io.tardisoft.jenkins.pipeline.gradle.step

import io.tardisoft.jenkins.pipeline.step.Step
import org.apache.commons.lang.StringUtils

/**
 * Base class for all steps that invoke gradle to accomplish their task
 */
abstract class GradleStep implements Serializable, Step {
    /**
     * DEFAULT JVM Args to use
     */
    public static final List<String> DEFAULT_JVM_ARGS = [
            "-Djava.awt.headless=true",
            "-Djava.net.preferIPv4Stack=true",
    ].asImmutable()

    /**
     * Default Gradle arguments to use
     */
    public static final List<String> DEFAULT_GRADLE_ARGS = [
            "-v"
    ].asImmutable()

    /**
     * Java VM arguments to set before running command
     */
    LinkedHashSet<String> jvmArgs = new LinkedHashSet<String>(DEFAULT_JVM_ARGS)
    /**
     * Gradle arguments to add to final command
     */
    LinkedHashSet<String> gradleArgs = new LinkedHashSet<String>(DEFAULT_GRADLE_ARGS)
    /**
     * True if build artifacts and dependencies should be placed in a repo local to the build
     */
    boolean localRepo = true

    /**
     * Build the final Gradle arguments line
     * @param additionalArgs Any additional arguments to add
     * @return A full argument command string to pass to gradle
     */
    String getGradleArgLine(List<String> additionalArgs) {
        LinkedHashSet<String> tempArgs = new LinkedHashSet<String>()
        if (getGradleArgs()) {
            tempArgs.addAll(getGradleArgs())
        }
        if (getLocalRepo()) {
            tempArgs.add("-Pgradle.repo.local=\$PWD/.repository")
        }
        if (additionalArgs != null) {
            tempArgs.addAll(additionalArgs)
        }
        return tempArgs.findAll { str -> StringUtils.isNotBlank(str) }.join(" ") ?: ""
    }

    /**
     * Build the string for java VM arguments, also known as Gradle options
     */
    String getGradleOpts() {
        return getJvmArgs()?.findAll { str -> StringUtils.isNotBlank(str) }?.join(" ") ?: ""
    }

    /**
     * Invoke the gradle command from the step, delegates to {@link #gradle(java.lang.Object, java.util.List)}
     * @param script Jenkinsfile script context
     * @param arg arguments to call gradle with
     * @param args additional arguments
     */
    void gradle(def script, String arg, String... args) {
        List<String> list = [arg]

        if (args != null) {
            list.addAll(args as List<String>)
        }

        gradle(script, list)
    }

    /**
     * Invoke the gradle command with the passed list of arguments
     *
     * If figure out if gradle wrapper is available use that if in the codebase,
     * otherwise assumes gradle is on the path
     *
     * @param script Jenkinsfile script context
     * @param args arguments to add to command
     */
    void gradle(def script, List<String> args) {
        String gradle = 'gradle'
        if (script.fileExists('gradlew')) {
            gradle = './gradlew'
        }

        // GRADLE_OPTS holds arguments for the JVM
        String gradleOptsValue = getGradleOpts()
        String gradleOpts
        if (gradleOptsValue) {
            gradleOpts = "export GRADLE_OPTS=\"${gradleOptsValue}\" &&"
        } else {
            gradleOpts = ''
        }

        String gradleArgStr = getGradleArgLine(args)
        List<String> fullCommandList = []
        fullCommandList.add(gradleOpts)
        fullCommandList.add(gradle)
        fullCommandList.add(gradleArgStr)

        String fullCommand = fullCommandList
                .findAll { str -> StringUtils.isNotBlank(str) }
                .join(" ")

        script.sh fullCommand
    }
}
