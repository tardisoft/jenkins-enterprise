package io.tardisoft.jenkins.pipeline.release

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification
import spock.lang.Unroll

class GitTagReleaseStrategySpec extends Specification {

    @Unroll
    def "test releaseVersion #pomVersion #tagPrefix produces #expectedVersion"(String pomVersion, String tagPrefix, String gitResult, String expectedVersion) {
        setup:
        JenkinsScript script = new JenkinsScript() {
            @Override
            def sh(def input) {
                return gitResult
            }
        }
        def pom = [version: pomVersion]
        GitTagReleaseStrategy strategy = new GitTagReleaseStrategy(tagPrefix: tagPrefix)

        when:
        String newVersion = strategy.releaseVersion(script, pom)

        then:
        newVersion == expectedVersion

        where:
        pomVersion        | tagPrefix | gitResult                                    | expectedVersion
        "1.2.3-SNAPSHOT"  | 'v'       | ''                                           | "1.2.0"
        "1.2.0-SNAPSHOT"  | 'v'       | 'v1.2.3'                                     | "1.2.4"
        "1.2.0-SNAPSHOT"  | 'ocf-'    | 'ocf-1.2.3'                                  | "1.2.4"
        "1.2.0-SNAPSHOT"  | 'v'       | 'v2.2.3\nv4.2.1\nv1.2.3\nv1.2.2\nv1.2.5'     | "1.2.6"
        "1.2.0-SNAPSHOT"  | 'ocf-'    | 'v2.2.3\nv4.2.1\nv1.2.3\nv1.2.2\nv1.2.5'     | "1.2.0"
        "1.2.4-SNAPSHOT"  | 'v'       | 'v2.'                                        | "1.2.0"
        "1.2.4-SNAPSHOT"  | ''        | '1.2.3\nhelloWorld'                          | "1.2.4"
        "1.2.4-SNAPSHOT"  | null      | '1.2.3\nhelloWorld'                          | "1.2.4"
        "1.2.4-SNAPSHOT"  | ''        | 'helloWorld-1.0.0\n1.2.3\nhelloWorld'        | "1.2.4"
        "1.2.4-SNAPSHOT"  | null      | 'helloWorld-1.3.0\n1.2.3\nhelloWorld'        | "1.2.4"
        "1.2.X-SNAPSHOT"  | 'v'       | 'v1.2.3'                                     | "1.2.4"
        "1.2.44-SNAPSHOT" | 'v'       | 'v1.2.3'                                     | "1.2.4"
        "1.2.X-SNAPSHOT"  | 'v'       | 'v1.2.10\nv1.2.11\nv1.2.12'                  | "1.2.13"
        "1.0.X"           | ''        | '1.0.10\n1.0.8\n1.0.9'                       | "1.0.11"
        "7.0.X-SNAPSHOT"  | 'v'       | 'v1.0.10\nv1.0.8\nv6.0.3'                    | "7.0.0"
        "1.2.X-SNAPSHOT"  | 'v'       | 'v1.2.10\nv1.2.11\nv1.2.12-SNAPSHOT'         | "1.2.13"
        "1.1.0-SNAPSHOT"  | null      | '1.0.10\n1.1.X.1\n1.1.X.2\n1.1.0.12\n1.1.14' | "1.1.15"
        "1.1.X-SNAPSHOT"  | null      | '1.0.10\n1.1.X.1\n1.1.X.2\n1.1.0.12\n1.1.14' | "1.1.15"
        "1.2.X-SNAPSHOT"  | null      | ''                                           | "1.2.0"
        "1.2.X-SNAPSHOT"  | null      | '1.0.10\n1.1.X.1\n1.1.X.2\n1.1.0.12\n1.1.14' | "1.2.0"
        "1.2.X-SNAPSHOT"  | null      | '1.0.10\n1.1.X.1\n1.1.X.2\n1.1.0.12\n1.2.14' | "1.2.15"
    }

    @Unroll
    def "test releaseVersion with no prefix #pomVersion results in #expectedVersion"(String pomVersion, String gitResult, String expectedVersion) {
        setup:
        JenkinsScript script = new JenkinsScript() {
            @Override
            def sh(def input) {
                return gitResult
            }
        }
        def pom = [version: pomVersion]
        GitTagReleaseStrategy strategy = new GitTagReleaseStrategy()

        when:
        String newVersion = strategy.releaseVersion(script, pom)

        then:
        newVersion == expectedVersion

        where:
        pomVersion        | gitResult                             | expectedVersion
        "1.2.3-SNAPSHOT"  | ''                                    | "1.2.0"
        "1.2.0-SNAPSHOT"  | '1.2.3'                               | "1.2.4"
        "1.2.0-SNAPSHOT"  | 'ocf-1.2.3'                           | "1.2.0"
        "1.2.0-SNAPSHOT"  | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'   | "1.2.6"
        "1.2.0-SNAPSHOT"  | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'   | "1.2.6"
        "1.2.4-SNAPSHOT"  | '2.'                                  | "1.2.0"
        "1.2.4-SNAPSHOT"  | '1.2.3\nhelloWorld'                   | "1.2.4"
        "1.2.4-SNAPSHOT"  | '1.2.3\nhelloWorld'                   | "1.2.4"
        "1.2.4-SNAPSHOT"  | 'helloWorld-1.0.0\n1.2.3\nhelloWorld' | "1.2.4"
        "1.2.4-SNAPSHOT"  | 'helloWorld-1.3.0\n1.2.3\nhelloWorld' | "1.2.4"
        "1.2.X-SNAPSHOT"  | '1.2.3'                               | "1.2.4"
        "1.2.44-SNAPSHOT" | '1.2.3'                               | "1.2.4"
        "1.2.X-SNAPSHOT"  | '1.2.10\n1.2.11\n1.2.12'              | "1.2.13"
        "1.0.X"           | '1.0.10\n1.0.8\n1.0.9'                | "1.0.11"
        "1.2.X-SNAPSHOT"  | '1.2.10\n1.2.11\n1.2.12-SNAPSHOT'     | "1.2.13"
    }
}