package io.tardisoft.jenkins.pipeline.release

import com.cloudbees.groovy.cps.NonCPS
import io.tardisoft.jenkins.pipeline.util.Version
import org.apache.commons.lang.StringUtils

/**
 * Extends from basic release strategy but calculates the release version from the git tags.
 * For instance if the git repo has the tags 1.2.0 1.3.0 1.4.0 and the pom has version 1.3.X
 * the new release will be 1.3.1
 */
class GitTagReleaseStrategy extends BasicReleaseStrategy implements ReleaseStrategy, Serializable {
    /**
     * Prefix to add to tags
     */
    String tagPrefix = ''
    /**
     * Previous version that was found, may be null
     */
    String lastVersion

    /**
     * Create the release version based off of the tags in the repository
     * @param script Jenkinsfile script context
     * @param pom pom to read to get version
     * @return New calculated version
     */
    @Override
    @NonCPS
    String releaseVersion(def script, def pom) {
        return releaseVersionCalculate(script, pom)
    }

    String releaseVersionCalculate(def script, def pom, String metaData = null, String separator = null) {
        String pomVersion = pom?.version
        String version = getNewVersion(script, tagPrefix, pomVersion, metaData, separator)
        script.echo "DEBUG: New Version = '${version}'"
        return version
    }

    /**
     * <B>WARNING WARNING WARNING</B>
     * !! This method is extremely fragile on Jenkins, edit at your own risk !!
     *
     *
     * @param script Script to use
     * @param tagPrefix tag prefix to use
     * @param pomVersion curring version of pom
     * @return new version as a String
     */
    String getNewVersion(
            def script,
            String tagPrefix,
            String pomVersion,
            String metaData = null,
            String separator = null) {
        Version version = Version.valueOf(pomVersion)
        version.metaData = metaData
        version.separator = separator
        script.sh("git fetch --tags")
        String gitStr
        try {
            gitStr = script.sh(
                    script: "git tag --list",
                    returnStdout: true)
        } catch (Exception e) {
            script.echo "WARNING: Git script failed. Assuming new release series " + e.message
            version.micro = 0
            return version as String
        }

        if (StringUtils.isBlank(gitStr)) {
            script.echo "WARNING: Git string returned empty. Assuming new release series"
            version.micro = 0
            return version as String
        } else {
            script.echo "DEBUG: Tag List = $gitStr"
        }

        String prefix = tagPrefix ?: ''

        final List<Version> versionList = new ArrayList<Version>()
        for (String tag : gitStr.split("\n")) {
            if (numberVersionMatches(version, tag, prefix) || textVersionMatches(version, tag, prefix)) {
                try {
                    String versionStr = tag.substring(prefix.length())
                    Version tagVersion = Version.valueOf(versionStr)
                    versionList.add(tagVersion)
                } catch (Exception ignore) {
                    script.echo "WARNING: Bad tag format \"${tag}\", skipping"
                }
            } else {
                script.echo "DEBUG: Skipping bad tag \"${tag}\""
            }
        }

        script.echo "DEBUG: Versions Size =  ${versionList.size()}"
        script.echo "DEBUG: Versions =  ${versionList}"

        final Set<Version> versionSet = new TreeSet<Version>(versionList)

        script.echo "DEBUG: Sorted Versions Size =  ${versionSet.size()}"
        script.echo "DEBUG: Sorted Versions =  ${versionSet}"

        Version latest = null
        if (!versionSet.isEmpty()) {
            latest = versionSet.last()
        }
        script.echo "DEBUG: Lastest Version = ${latest}"

        if (latest != null) {
            script.echo "DEBUG: Latest not null, doing calc on new version"
            lastVersion = latest as String

            // Increment the minor release
            if (latest.isNumberPattern) {
                if (latest.micro == null) {
                    latest.micro = 0
                } else {
                    latest.micro++
                }
            } else if (latest.isTextPattern) {
                latest.minor++
            }

            // Remove any metadata
            latest.metaData = metaData
            latest.separator = separator
            script.echo "DEBUG: Using ${latest.toString()}"
            return latest.toString()
        } else {
            script.echo "DEBUG: Latest null, Using micro=0"
            version.micro = 0
            script.echo "DEBUG: Using ${version.toString()}"
            return version.toString()
        }
    }

    private static boolean numberVersionMatches(Version version, String tag, String prefix) {
        version.isNumberPattern && tag.startsWith("${prefix}${version.major}.${version.minor}")
    }

    private static boolean textVersionMatches(Version version, String tag, String prefix) {
        !version.isNumberPattern && version.isTextPattern && tag.startsWith("${prefix}${version.major}")
    }
}
