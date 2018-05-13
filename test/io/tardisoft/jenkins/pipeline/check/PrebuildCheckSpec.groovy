package io.tardisoft.jenkins.pipeline.check

import hudson.PluginManager
import hudson.PluginWrapper
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import jenkins.model.Jenkins
import spock.lang.Specification

class PrebuildCheckSpec extends Specification {

    JenkinsScript script = new JenkinsScript()
    Jenkins jenkins = Mock(Jenkins)
    PluginManager pluginManager = Mock(PluginManager)

    def "test skip validatePlugins"() {
        setup:
        script.env.SKIP_VALIDATE_PLUGINS = true
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []

        when:
        PrebuildCheck.validateJenkins(script, jenkins)

        then:
        noExceptionThrown()
    }

    def "test invalid state"() {
        setup:
        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> []

        when:
        PrebuildCheck.validateJenkins(script, jenkins)

        then:
        thrown(IllegalStateException)
    }

    def "test valid state"() {
        setup:
        PluginWrapper pluginWrapperCheckstyle = Mock(PluginWrapper)
        pluginWrapperCheckstyle.getShortName() >> 'checkstyle'
        pluginWrapperCheckstyle.getVersion() >> '3.48'

        PluginWrapper pluginWrapperCobertura = Mock(PluginWrapper)
        pluginWrapperCobertura.getShortName() >> 'cobertura'
        pluginWrapperCobertura.getVersion() >> '1.10'

        jenkins.getPluginManager() >> pluginManager
        pluginManager.getPlugins() >> [pluginWrapperCheckstyle, pluginWrapperCobertura]

        when:
        PrebuildCheck.validateJenkins(script, jenkins, 'pluginVersionsTest')

        then:
        noExceptionThrown()
    }

}