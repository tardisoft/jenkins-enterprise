package io.tardisoft.jenkins.pipeline.maven.step

import io.tardisoft.jenkins.pipeline.step.Step
import org.apache.commons.lang.StringUtils

/**
 * Base class for all steps that invoke maven to accomplish their task
 */
abstract class MavenStep implements Serializable, Step {
    /**
     * DEFAULT JVM Args to use
     */
    public static final List<String> DEFAULT_JVM_ARGS = [
            "-Djava.awt.headless=true",
            "-Djava.net.preferIPv4Stack=true",
    ].asImmutable()

    /**
     * Default Maven arguments to use
     */
    public static final List<String> DEFAULT_MAVEN_ARGS = [
            "-nsu",
            "--batch-mode",
            "-e",
            "-V"
    ].asImmutable()

    /**
     * Java VM arguments to set before running command
     */
    LinkedHashSet<String> jvmArgs = new LinkedHashSet<String>(DEFAULT_JVM_ARGS)
    /**
     * Maven arguments to add to final command
     */
    LinkedHashSet<String> mavenArgs = new LinkedHashSet<String>(DEFAULT_MAVEN_ARGS)
    /**
     * True if build artifacts and dependencies should be placed in a repo local to the build
     */
    boolean localRepo = true

    /**
     * Build the final Maven arguments line
     * @param additionalArgs Any additional arguments to add
     * @return A full argument command string to pass to maven
     */
    String getMavenArgLine(List<String> additionalArgs) {
        LinkedHashSet<String> tempArgs = new LinkedHashSet<String>()
        if (getMavenArgs()) {
            tempArgs.addAll(getMavenArgs())
        }
        if (getLocalRepo()) {
            tempArgs.add("-Dmaven.repo.local=\$PWD/.repository")
        }
        if (additionalArgs != null) {
            tempArgs.addAll(additionalArgs)
        }
        return tempArgs.findAll { str -> StringUtils.isNotBlank(str) }.join(" ") ?: ""
    }

    /**
     * Build the string for java VM arguments, also known as Maven options
     */
    String getMavenOpts() {
        return getJvmArgs()?.findAll { str -> StringUtils.isNotBlank(str) }?.join(" ") ?: ""
    }

    /**
     * Invoke the maven command from the step, delegates to {@link #mvn(java.lang.Object, java.util.List)}
     * @param script Jenkinsfile script context
     * @param arg arguments to call maven with
     * @param args additional arguments
     */
    void mvn(def script, String arg, String... args) {
        List<String> list = [arg]

        if (args != null) {
            list.addAll(args as List<String>)
        }

        mvn(script, list)
    }

    /**
     * Invoke the maven command with the passed list of arguments
     *
     * If figure out if maven wrapper is available use that if in the codebase,
     * otherwise assumes mvn is on the path
     *
     * @param script Jenkinsfile script context
     * @param args arguments to add to command
     */
    void mvn(def script, List<String> args) {
        String mvn = 'mvn'
        if (script.fileExists('mvnw')) {
            mvn = './mvnw'
        }

        // MAVEN_OPTS holds arguments for the JVM
        String mavenOptsValue = getMavenOpts()
        String mavenOpts
        if (mavenOptsValue) {
            mavenOpts = "export MAVEN_OPTS=\"${mavenOptsValue}\" &&"
        } else {
            mavenOpts = ''
        }

        String mavenArgStr = getMavenArgLine(args)
        List<String> fullCommandList = []
        fullCommandList.add(mavenOpts)
        fullCommandList.add(mvn)
        fullCommandList.add(mavenArgStr)

        String fullCommand = fullCommandList
                .findAll { str -> StringUtils.isNotBlank(str) }
                .join(" ")

        script.sh fullCommand
    }
}
