package io.tardisoft.jenkins.pipeline

import hudson.PluginManager
import io.tardisoft.jenkins.pipeline.check.Check
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import jenkins.model.Jenkins
import spock.lang.Specification

class SimplePipelineSpec extends Specification {

    def "test simple call"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
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
    }

    def "test simple call no precheck"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.prebuildChecks = checks

        expect:
        pipeline.run(script)

        where:
        checks << [null, []]
    }

    def "test simple call force pass"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true
        pipeline.prebuildChecks = [new ForceCheck(true)]

        expect:
        pipeline.run(script)
    }

    def "test simple call full build"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins

        expect:
        pipeline.run(script)
    }

    def "test simple call no ENV"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.env = [:]

        expect:
        pipeline.run(script)
    }

    def "test simple call with body"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true

        expect:
        pipeline.run(script) {
            pipeline.skipPullRequestStatus = true
        }
    }

    def "test simple call with body map"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = true

        expect:
        pipeline.run(script, [foo: false])
    }

    def "test simple call with body map skip pull request"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = false

        expect:
        pipeline.run(script, [foo: false])
    }

    def "test simple call prebuild check"() {
        setup:
        SimpleJenkinsPipeline pipeline = new SimpleJenkinsPipeline()
        JenkinsScript script = Spy(JenkinsScript)
        script.env.SKIP_VALIDATE_PLUGINS = true
        Jenkins jenkins = Mock(Jenkins)
        PluginManager pluginManager = Mock(PluginManager)
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []
        pipeline.jenkinsInstance = jenkins
        pipeline.skipPullRequestStatus = false
        pipeline.prebuildChecks = [new ForceCheck(false)]

        expect:
        pipeline.run(script, [foo: false])
    }

    class ForceCheck implements Check {

        ForceCheck(result){
            this.result = result
        }

        boolean result

        @Override
        boolean run(Object script, Jenkins jenkins) {
            return result
        }
    }

}