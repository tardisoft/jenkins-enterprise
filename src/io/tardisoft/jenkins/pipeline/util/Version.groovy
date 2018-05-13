package io.tardisoft.jenkins.pipeline.util

import com.cloudbees.groovy.cps.NonCPS
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.builder.HashCodeBuilder

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Represents a semantic version for numeric versions
 * ${major}.${minor}.${micro}${separator}${metaData}*
 * Supports:
 * 1.0.0
 * 1.0.X-SNAPSHOT
 * 1.0.x-SNAPSHOT
 * 1.0.0-metaData
 * 1.0.0.metaData
 *
 * Also supports a semantic version for textual versions
 * ${major}.${minor}${separator}${metaData}*
 * Supports:
 * Text.1-metaData
 * Text.1.metaData
 * Text.1
 *
 * @see <a href="http://semver.org/">Semantic Versioning</a>
 *
 * Supports natural ordering
 */
class Version implements Comparable<Version>, Serializable {
    private static
    final Pattern numberPattern = Pattern.compile('(?<major>\\d+)\\.(?<minor>\\d+)(\\.(?<micro>[0-9xX]+))((?<separator>[-\\.]{1})(?<metaData>.*))?')
    private static
    final Pattern textPattern = Pattern.compile('(?<major>.*)\\.(?<minor>\\d+)((?<separator>[-\\.]{1})(?<metaData>.*))?')

    String major
    Integer minor
    Integer micro = 0
    String metaData
    String separator = '-'
    boolean isNumberPattern = false
    boolean isTextPattern = false

    /**
     * Parse a version from a string
     * @param str version string
     * @return Version of the string
     */
    @NonCPS
    static Version valueOf(String str) {
        Matcher numberMatcher = numberPattern.matcher(str)
        Matcher textMatcher = textPattern.matcher(str)
        if (!numberMatcher?.matches() && !textMatcher?.matches()) {
            throw new IllegalArgumentException("Invalid version string: " + str)
        }

        Integer micro = null
        if (numberMatcher.matches()) {
            try {
                micro = Integer.valueOf(numberMatcher.group('micro'))
            } catch (Exception ignore) {
            }
        }

        if (numberMatcher.matches()) {
            return new Version(
                    major: numberMatcher.group('major'),
                    minor: Integer.valueOf(numberMatcher.group('minor')),
                    micro: micro,
                    separator: numberMatcher.group('separator'),
                    metaData: numberMatcher.group('metaData'),
                    isNumberPattern: numberMatcher?.matches(),
                    isTextPattern: textMatcher?.matches())
        } else if (textMatcher.matches()) {
            return new Version(
                    major: textMatcher.group('major'),
                    minor: Integer.valueOf(textMatcher.group('minor')),
                    separator: textMatcher.group('separator'),
                    metaData: textMatcher.group('metaData'),
                    isNumberPattern: numberMatcher?.matches(),
                    isTextPattern: textMatcher?.matches())
        }
    }

    /**
     * MUST BE NonCPS or doesn't actually create string
     *
     * {@inheritDoc}
     */
    @NonCPS
    String toString() {
        String retVal
        if (isNumberPattern || (major ==~ /\d+/)) {
            String nullSafeMinor = StringUtils.defaultIfBlank(micro?.toString(), '0')
            retVal = "${major}.${minor}.${nullSafeMinor}"
        } else {
            retVal = "${major}.${minor}"
        }
        if (StringUtils.isNotBlank(metaData)) {
            retVal = "${retVal}${separator}${metaData}"
        }
        return retVal.toString()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonCPS
    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        Version version = (Version) o

        if (major != version.major) {
            return false
        }
        if (minor != version.minor) {
            return false
        }
        if (isNumberPattern && micro != version.micro) {
            return false
        }
        if (metaData != version.metaData) {
            return false
        }

        return true
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonCPS
    int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(major)
                .append(minor)
                .append((isNumberPattern || (major ==~ /\d+/)) ? micro : "")
                .append(metaData)
                .toHashCode()

    }

    /**
     * MUST BE NonCPS or doesn't actually compare, who knew?
     *
     * {@inheritDoc}
     */
    @NonCPS
    @Override
    int compareTo(Version o) {
        String thisMetaData = (metaData == null) ? '' : metaData
        String thatMetaData = (o.metaData == null) ? '' : o.metaData
        int retVal
        if (major != o.major) {
            retVal = major <=> o.major
        } else if (minor != o.minor) {
            retVal = minor - o.minor
        } else if (isNumberPattern || (major ==~ /\d+/) && micro != o.micro) {
            retVal = (micro ?: 0) - (o.micro ?: 0)
        } else {
            retVal = thisMetaData <=> thatMetaData
        }
        return retVal
    }

    @NonCPS
    String toStringFull() {
        return "Version{" +
                "major='" + major + '\'' +
                ", minor=" + minor +
                ", micro=" + micro +
                ", metaData='" + metaData + '\'' +
                ", separator='" + separator + '\'' +
                ", isNumberPattern=" + isNumberPattern +
                ", isTextPattern=" + isTextPattern +
                '}'
    }
}