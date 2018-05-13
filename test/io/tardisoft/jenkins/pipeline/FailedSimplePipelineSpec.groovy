package io.tardisoft.jenkins.pipeline

import hudson.PluginManager
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

class FailedSimplePipelineSpec extends Specification {

    @Unroll
    def "test simple call #status"() {
        setup:
        FailedSimpleJenkinsPipeline pipeline = new FailedSimpleJenkinsPipeline(status)
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true

        expect:
        pipeline.run(script)

        where:
        status << ["SUCCESS", "FAILED", "ABORTED"]
    }
}