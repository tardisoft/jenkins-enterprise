package io.tardisoft.jenkins.pipeline.release

/**
 * Extensible strategy
 */
interface ReleaseStrategy {

    /**
     * Calculate a new snapshot version based on the pom
     * @param script Jenkinsfile script context
     * @param pom Pom to get version from
     * @return New version that should be used to the snapshot build
     */
    String snapshotVersion(def script, def pom)

    /**
     * Calculate a new release version based on the pom
     * <B>NOTE</B> Release build MUST be universally unique and can not be released again, artifactory will reject it
     * @param script Jenkinsfile script context
     * @param pom Pom to get the version from
     * @return New version that should be used in the release build
     */
    String releaseVersion(def script, def pom)
}