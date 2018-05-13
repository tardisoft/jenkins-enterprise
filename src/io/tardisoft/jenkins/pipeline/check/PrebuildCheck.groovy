package io.tardisoft.jenkins.pipeline.check

import hudson.PluginWrapper
import hudson.model.Cause
import jenkins.model.Jenkins
import org.apache.commons.lang.StringUtils

/**
 * Prebuild checks to verify the Jenkins environment before the build
 * <p>
 * The script will validate the instance of Jenkins it is running is
 * has all the necessary plugins it needs to execute.  This list is stored
 * in the resources/pluginVersions file.
 * <p>
 * Format is:
 * <p>
 * ${pluginShortName}\[:${pluginVersion}]
 * <p>
 * Plugin version is optional
 */
class PrebuildCheck implements Check, Serializable {
    /**
     * List of users we shouldn't trigger a build for
     */
    List<String> skipUsers = []

    /**
     * The Jenkins plugin version validator, ensures Jenkins is running with versions
     * of plugins we are expecting
     */
    boolean validateJenkinsPlugins = false

    /**
     * {@inheritDoc }
     */
    @Override
    boolean run(def script, Jenkins jenkins) {
        if (validateJenkinsPlugins) {
            validateJenkins(script, jenkins)
        }

        def build = script.currentBuild.rawBuild
        def cause = build?.getCause(Cause.UserIdCause.class)
        def USER_ID = cause?.getUserId()

        if (USER_ID != null && skipUsers.contains(USER_ID)) {
            script.echo "Will not build commits by ${USER_ID}"
            script.currentBuild.result = 'ABORTED'
            return false
        }
        return true
    }

    /**
     * Check to ensure the version of plugins in Jenkins match those that we expect, aborts the build if they don't
     * @param script Jenkinsfile script context
     * @param jenkins Jenkins instance to check
     * @param pluginVersionsFilePath path to plugin versions file to use for checks
     */
    static void validateJenkins(def script, Jenkins jenkins, String pluginVersionsFilePath = 'pluginVersions') {
        if (script.env.SKIP_VALIDATE_PLUGINS) {
            return
        }
        String pluginVersionsFile = script.libraryResource(pluginVersionsFilePath)
        if (StringUtils.isBlank(pluginVersionsFile)) {
            throw new IllegalStateException("Failed to locate pluginVersions configuration file")
        }
        String[] pluginVersions = pluginVersionsFile.split("\n")
        List<PluginWrapper> plugins = jenkins.getPluginManager().getPlugins()
        List<String> missingPlugins = []
        for (String pluginStr : pluginVersions) {
            String[] plugin = pluginStr.split(":")
            String pluginShortName = plugin[0]
            String pluginVersion = null
            if (plugin.length == 2) {
                pluginVersion = plugin[1]
            }
            boolean found = false
            for (PluginWrapper pluginWrapper : plugins) {
                if (pluginWrapper.getShortName() == pluginShortName
                        && (pluginVersion == null || pluginVersion == pluginWrapper.getVersion())) {
                    found = true
                    break
                }
            }
            if (!found) {
                missingPlugins.add(pluginStr)
            }
        }

        if (!missingPlugins.isEmpty()) {
            throw new IllegalStateException("Invalid Jenkins Plugin configuration missing plugins:\n\t${missingPlugins.join("\n\t")}")
        }
    }
}