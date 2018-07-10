#!/usr/bin/groovy
import io.tardisoft.jenkins.pipeline.gradle.GradleJenkinsPipeline
import io.tardisoft.jenkins.pipeline.notify.DatadogNotifyExternalStep
import io.tardisoft.jenkins.pipeline.release.GitTagReleaseStrategy

/**
 * Used to build libraries using the minimum amount of information necessary to have it built, tested and released
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def overrideConfig = [:]
    if (body instanceof Map) {
        overrideConfig = body
    } else if (body != null) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = overrideConfig
        body()
    }

    def config = [
            node                 : '',
            branchesToRelease    : ['master'],
            updateStrategy       : new GitTagReleaseStrategy(),
            gitCredentialsId     : 'github',
            gitOauthCredentialsId: 'gitoauth',
            deploySite           : false, //github gh-pages site
            generateJavadoc      : false,
            runQualityGate       : false
    ]
    config.putAll(overrideConfig)

    GradleJenkinsPipeline pipeline = new GradleJenkinsPipeline()
    pipeline.run(this) {
        pipeline.nodeLabel = config.node
        pipeline.releaseBranches = (List<String>) (config.branchesToRelease instanceof Collection ? config.branchesToRelease : [config.branchesToRelease])
        pipeline.gitCredentialsId = config.gitCredentialsId
        pipeline.gitOauthCredentialsId = config.gitOauthCredentialsId
        pipeline.notifyExternalStep = [new DatadogNotifyExternalStep()]
    }

}

