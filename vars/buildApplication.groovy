#!/usr/bin/groovy
import io.tardisoft.jenkins.pipeline.maven.MavenJenkinsPipeline
import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenGhPagesDeployStep
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
            node             : '',
            branchesToRelease: 'master',
            updateStrategy   : new GitTagReleaseStrategy(),
            gitCredentialsId : 'github',
            rootPom          : 'pom.xml',
            parentPom        : 'pom.xml',
            deploySite       : false, //github gh-pages site
            generateMavenSite: false,
            generateJavadoc  : false,
            runQualityGate   : false
    ]
    config.putAll(overrideConfig)

    MavenJenkinsPipeline pipeline = new MavenJenkinsPipeline(runQualityGate: config.runQualityGate)

    pipeline.run(this) {
        pipeline.nodeLabel = config.node
        pipeline.releaseBranches = (List<String>) (config.branchesToRelease instanceof Collection ? config.branchesToRelease : [config.branchesToRelease])
        pipeline.gitCredentialsId = config.gitCredentialsId
        pipeline.updateVersionStep.updateStrategy = config.updateStrategy
        pipeline.rootPom = config.rootPom
        pipeline.parentPom = config.parentPom
        pipeline.deploySite = config.deploySite
        pipeline.deploySteps = [new MavenGhPagesDeployStep(generateMavenSite: config.generateMavenSite, generateJavadoc: config.generateJavadoc)]
    }
}

