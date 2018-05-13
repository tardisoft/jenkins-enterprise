package io.tardisoft.jenkins.pipeline.release

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class BasicReleaseStrategySpec extends Specification {

    def "test releaseVersion"(String pomVersion, int buildNumber, String expectedVersion) {
        setup:
        JenkinsScript script = new JenkinsScript()
        script.currentBuild.number = buildNumber
        def pom = [version: pomVersion]
        BasicReleaseStrategy strategy = new BasicReleaseStrategy()

        when:
        String newVersion = strategy.releaseVersion(script, pom)

        then:
        newVersion == expectedVersion

        where:
        pomVersion       | buildNumber | expectedVersion
        "1.2.3-SNAPSHOT" | 1           | "1.2.3.1"
        "1.2-SNAPSHOT"   | 3           | "1.2.3"
    }

    def "test snapshot Version"(String branchName, String pomVersion, String expectedVewVersion) {
        setup:
        JenkinsScript script = new JenkinsScript()
        script.env.BRANCH_NAME = branchName
        def pom = [version: pomVersion]
        BasicReleaseStrategy strategy = new BasicReleaseStrategy()

        when:
        String newVersion = strategy.snapshotVersion(script, pom)

        then:
        newVersion == expectedVewVersion

        where:
        branchName                        | pomVersion        | expectedVewVersion
        'feature-branch'                  | '1.0.0-SNAPSHOT'  | '1.0.0.feature-branch-SNAPSHOT'
        'refactor/userid/43.0.2-SNAPSHOT' | '43.0.2-SNAPSHOT' | '43.0.2.refactor_userid_43.0.2-SNAPSHOT'
    }

    def "test badBranchName Version"() {
        setup:
        JenkinsScript script = new JenkinsScript()
        script.env.BRANCH_NAME = 'feature-branch'
        def pom = [version: '1.0.0-SNAPSHOT']
        BasicReleaseStrategy strategy = new BasicReleaseStrategy()

        when:
        String newVersion = strategy.snapshotVersion(script, pom)

        then:
        newVersion == '1.0.0.feature-branch-SNAPSHOT'
    }

    def "test update branch name"(String branchName, String expectedValue) {
        setup:
        BasicReleaseStrategy strategy = new BasicReleaseStrategy()

        when:
        String value = strategy.updateBranchName(branchName)

        then:
        value == expectedValue

        where:
        branchName          | expectedValue
        '1.0.0-SNAPSHOT'    | '1.0.0-SNAPSHOT'
        'test-SNAPSHOT'     | 'test-SNAPSHOT'
        'path/test/value'   | 'path_test_value'
        'path\\test\\value' | 'path_test_value'
        'weird<>'           | 'weird__'
        'weird"|?*'         | 'weird____'
    }

}