package io.tardisoft.jenkins.pipeline

import io.tardisoft.jenkins.pipeline.check.Check
import io.tardisoft.jenkins.pipeline.check.PrebuildCheck
import io.tardisoft.jenkins.pipeline.step.CheckoutStep
import io.tardisoft.jenkins.pipeline.step.CleanStep
import io.tardisoft.jenkins.pipeline.step.PropertiesStep
import io.tardisoft.jenkins.pipeline.util.Common
import jenkins.model.Jenkins

import java.time.Duration

/**
 * Base Pipeline configuration, setup basic stages and behavior
 */
abstract class AbstractJenkinsPipeline implements Serializable {

    /**
     * Node to run the build on
     */
    String nodeLabel = ''

    /**
     * Add ability to specify environment variables on the node
     */
    Map<String, String> env = [:]

    /**
     * List of branches that are considered 'release' branches
     */
    List<String> releaseBranches = []

    /**
     * Jenkins secrets ID to use for git credentials
     */
    String gitCredentialsId = 'github'

    /**
     * Jenkins secrets ID to use for git oauth credentials
     */
    String gitOauthCredentialsId = 'gitoauth'

    /**
     * Git hub context to use for pull requests
     */
    String gitHubContext = 'continuous-integration/jenkins/pr-merge'

    /**
     *  Git hub api url
     */
    String gitHubApiUrl = 'https://api.github.com'

    /**
     * Jenkins instance to call back to
     */
    volatile transient Jenkins jenkinsInstance = Jenkins.getInstance()

    /**
     * If true will skip attempting to update status on related github pull request
     */
    boolean skipPullRequestStatus = false

    /**
     * Setup the jobs properties
     */
    PropertiesStep propertiesStep = new PropertiesStep()

    /**
     * Job prebuild check, like jenkins verification etc
     */
    List<Check> prebuildChecks = [new PrebuildCheck()]

    /**
     * Checkout the source code
     */
    CheckoutStep checkoutStep = new CheckoutStep()

    /**
     * Clean step for wiping the workspace
     */
    CleanStep cleanStep = new CleanStep()

    /**
     * Duration of the build timeout, will error build if timeout expires
     * Set the timeout to 'null' to disable the timeout
     */
    Duration buildTimeout = Duration.ofMinutes(60)

    /**
     * Runs the build, should be called directly from Jenkinsfile
     * @param script the script context, usually 'this' in a Jenkinsfile
     * @param body closure or map of additional build options to run before build
     */
    void run(def script, def body) {
        if (!skipPullRequestStatus) {
            skipPullRequestStatus = !new Common(script).isPullRequest()
        }

        // evaluate the body block, and collect configuration into the object
        Map overrideConfig = [:]
        if (body instanceof Map) {
            overrideConfig = body
        } else if (body != null) {
            body.resolveStrategy = Closure.DELEGATE_FIRST
            body.delegate = overrideConfig
            body()
        }
        for (String key : overrideConfig.keySet()) {
            def value = overrideConfig.get(key)
            if (this.hasProperty(key)) {
                this."$key" = value
            }
        }
        run(script)
    }

    /**
     * Runs the build, should be called directly from Jenkinsfile
     * @param script the script context, usually 'this' in a Jenkinsfile
     */
    void run(def script) {
        setup(script)
        boolean shouldBuild = true
        stage(script, 'Prebuild Checks') {
            shouldBuild = prebuildChecks(script)
        }
        if (!shouldBuild) {
            return
        }
        script.timestamps {
            node(script, nodeLabel) {
                if (env) {
                    script.withEnv(env) {
                        pipeline(script)
                    }
                } else {
                    pipeline(script)
                }
            }
        }
    }

    /**
     * The main pipeline, runs all the stages
     */
    void pipeline(def script) {
        def closure = {

            stage(script, 'Checkout') {
                checkout(script)
            }

            try {
                script.withMaven(maven: 'Maven 3.3.9') {
                    try {
                        stage(script, 'Build and Test') {
                            buildAndTest(script)
                        }
                    } catch (Exception e) {
                        script.echo "Error building ${e?.getMessage()}"
                        script.echo "${e?.stackTrace?.toString()}"
                    } finally {
                        if (script.currentBuild.result != 'ABORTED') {
                            stage(script, 'Publish Build Results') {
                                publish(script)
                            }
                        }
                    }

                    stage(script, 'Quality Gate') {
                        qualityGate(script)
                    }

                    stage(script, 'Tag and Deploy') {
                        deploy(script)
                    }

                    stage(script, 'Publish Site') {
                        site(script)
                    }

                    stage(script, 'Notify External') {
                        notifyExternal(script)
                    }
                }
            } finally {
                stage(script, "Cleanup") {
                    cleanup(script)
                }
            }
        }
        if (buildTimeout) {
            timeout(script, buildTimeout.toMinutes() as int, 'MINUTES', closure)
        } else {
            closure()
        }
    }

    /**
     * Sends a message and state to the github pull request
     * Errors are caught and logged, execution continues.
     *
     * @param script Jenkinsfile script context
     * @param message the message to display
     * @param state the new state of the pull request
     */
    void setGitHubPullRequestStatus(def script, def message, def state) {
        if (skipPullRequestStatus) {
            return
        }
        try {
            script.echo "Updating GitHub pull request message:'${message}', state: '${state}'"
            script.githubNotify description: message, status: state, context: gitHubContext, gitApiUrl: gitHubApiUrl
        } catch (Exception e) {
            StringWriter stacktrace = new StringWriter()
            e.printStackTrace(new PrintWriter(stacktrace))
            script.echo "Error setting github pull request: ${e.getMessage()}\n${stacktrace.toString()}"
            skipPullRequestStatus = true
        }
    }

    /**
     * Create a node and update the github pull request message indication a node is being created
     * @param script Jenkinsfile script context
     * @param nodeLabel label of the node to launch on
     * @param body closure body to execute on the node
     */
    void node(def script, def nodeLabel, Closure<?> body = null) {
        setGitHubPullRequestStatus script, "Waiting for node on ${nodeLabel}", 'PENDING'
        script.node(nodeLabel, body)
    }

    /**
     * Execute a stage, wraps the Jenkinsfile stage method
     * Updates the pull request to indicate the stage is being executed
     *
     * @param script Jenkinsfile script context
     * @param name name of the stage
     * @param body closure body to execute in the stage
     */
    void stage(def script, def name, Closure<?> body = null) {
        script.stage(name) {
            setGitHubPullRequestStatus script, "Executing stage: ${name}", 'PENDING'
            body?.call()
        }
    }

    /**
     * wraps a call in a timeout, wraps the Jenkinsfile timeout method
     * Executes the code inside the block with a determined time out limit. If the time limit is reached, an exception is thrown, which leads in aborting the build (unless it is caught and processed somehow).
     * time
     * Type: int
     *
     * unit (optional)
     * Values:
     *
     * NANOSECONDS
     * MICROSECONDS
     * MILLISECONDS
     * SECONDS
     * MINUTES
     * HOURS
     * DAYS
     *
     *
     * @param script Jenkinsfile script context
     * @param time int value of time default value is 60
     * @param unit String value of time units, see above for values default to 'MINUTES'
     * @param body closure body to execute in the timeout block
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    void timeout(def script, int time = 60, String units = 'MINUTES', Closure<?> body = null) {
        script.timeout(time: time, unit: units) {
            body?.call()
        }
    }

    /**
     * Setup the job configuration such as properties, log etc
     * @param script Jenkinsfile script context
     */
    void setup(def script) {
        propertiesStep?.run(script)
    }

    /**
     * Run the prebuild checks
     * @param script Jenkinsfile script context
     */
    boolean prebuildChecks(def script) {
        if (prebuildChecks?.isEmpty()) {
            return true
        }
        return prebuildChecks.every { it.run(script, jenkinsInstance) }
    }

    /**
     * Checkout the repository
     * @param script Jenkinsfile script context
     */
    void checkout(def script) {
        cleanStep?.run(script)
        checkoutStep?.run(script)
    }

    /**
     * Invoke build actions, clean install etc
     * @param script Jenkinsfile script context
     */
    abstract void buildAndTest(def script)

    /**
     * Invoke the quality control actions, sonar etc
     * @param script Jenkinsfile script context
     */
    abstract void qualityGate(def script)

    /**
     * Notify some other service about this build
     * @param script
     */
    void notifyExternal(def script) {}

    /**
     * Publish Jenkins results, units tests, SCA analysis etc back to Jenkins
     * @param script Jenkinsfile script context
     */
    abstract void publish(def script)

    /**
     * Deploy to external systems, such as dtr or artifactory
     * @param script Jenkinsfile script context
     */
    abstract void deploy(def script)

    abstract void site(def script)

    /**
     * Run any final cleanup actions, this stage is always executed
     * @param script Jenkinsfile script context
     */
    abstract void cleanup(def script)
}
