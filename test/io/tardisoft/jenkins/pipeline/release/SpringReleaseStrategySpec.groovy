package io.tardisoft.jenkins.pipeline.release

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification
import spock.lang.Unroll


class SpringReleaseStrategySpec extends Specification {

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
        SpringReleaseStrategy strategy = new SpringReleaseStrategy()

        when:
        String newVersion = strategy.releaseVersion(script, pom)

        then:
        newVersion == expectedVersion

        where:
        pomVersion                         | gitResult                                                                   | expectedVersion
        "1.0.3-SNAPSHOT"                   | ''                                                                          | "1.0.0.RELEASE"
        "1.0.3-SNAPSHOT"                   | 'bad'                                                                       | "1.0.0.RELEASE"
        "1.2.0-SNAPSHOT"                   | '1.2.3'                                                                     | "1.2.4.RELEASE"
        "1.2.0-SNAPSHOT"                   | 'ocf-1.2.3'                                                                 | "1.2.0.RELEASE"
        "1.2.0-SNAPSHOT"                   | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'                                         | "1.2.6.RELEASE"
        "4.2.0-SNAPSHOT"                   | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'                                         | "4.2.2.RELEASE"
        "3.2.0-SNAPSHOT"                   | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'                                         | "3.2.0.RELEASE"
        "1.2.0-SNAPSHOT"                   | '2.2.3\n4.2.1\n1.2.3\n1.2.2\n1.2.5'                                         | "1.2.6.RELEASE"
        "1.2.4-SNAPSHOT"                   | '2.'                                                                        | "1.2.0.RELEASE"
        "1.2.4-SNAPSHOT"                   | '1.2.3\nhelloWorld'                                                         | "1.2.4.RELEASE"
        "1.2.4-SNAPSHOT"                   | 'helloWorld-1.0.0\n1.2.3\nhelloWorld'                                       | "1.2.4.RELEASE"
        "1.2.4-SNAPSHOT"                   | 'helloWorld-1.3.0\n1.2.3\nhelloWorld'                                       | "1.2.4.RELEASE"
        "1.2.X-SNAPSHOT"                   | '1.2.3'                                                                     | "1.2.4.RELEASE"
        "1.2.44-SNAPSHOT"                  | '1.2.3'                                                                     | "1.2.4.RELEASE"
        "1.2.X-SNAPSHOT"                   | '1.2.10\n1.2.11\n1.2.12'                                                    | "1.2.13.RELEASE"
        "1.0.X"                            | '1.0.10\n1.0.8\n1.0.9'                                                      | "1.0.11.RELEASE"
        "1.2.X-SNAPSHOT"                   | '1.2.10\n1.2.11\n1.2.12-SNAPSHOT'                                           | "1.2.13.RELEASE"
        "1.2.4.RELEASE-SNAPSHOT"           | '1.2.10\n1.2.11\n1.2.12-SNAPSHOT'                                           | "1.2.13.RELEASE"
        "1.2.4.RELEASE-SNAPSHOT"           | '2.0.0.RELEASE'                                                             | "1.2.0.RELEASE"
        "1.2.4.SNAPSHOT"                   | '2.0.0.RELEASE'                                                             | "1.2.0.RELEASE"
        "1.2.4.RELEASE-SNAPSHOT"           | '1.2.7.RELEASE'                                                             | "1.2.8.RELEASE"
        "Brussels-SR5.26.RELEASE-SNAPSHOT" | 'Brussels-SR5.25.RELEASE\nBrussels-SR5.27.RELEASE'                          | "Brussels-SR5.28.RELEASE"
        "Brussels-SR5.25.RELEASE-SNAPSHOT" | 'Brussels-SR5.25.RELEASE\nBrussels-SR5.27.RELEASE\nBrussels-SR5.26.RELEASE' | "Brussels-SR5.28.RELEASE"
        "Brussels-SR5.24.RELEASE-SNAPSHOT" | 'Brussels-SR5.25.RELEASE\nBrussels-SR5.27.RELEASE\nBrussels-SR6.1.RELEASE'  | "Brussels-SR5.28.RELEASE"
        "Brussels-SR5.23.RELEASE-SNAPSHOT" | 'Brussels-SR5.25.RELEASE'                                                   | "Brussels-SR5.26.RELEASE"
        "Brussels-SR5.23.SNAPSHOT"         | 'Brussels-SR5.25.RELEASE'                                                   | "Brussels-SR5.26.RELEASE"
        "Brussels-SR5.22.RELEASE-SNAPSHOT" | '1.2.7.RELEASE'                                                             | "Brussels-SR5.22.RELEASE"
        "Brussels-SR5.21.RELEASE-SNAPSHOT" | ''                                                                          | "Brussels-SR5.21.RELEASE"
        "Brussels-SR5.21-SNAPSHOT"         | ''                                                                          | "Brussels-SR5.21.RELEASE"
        "Brussels-SR5.21.SNAPSHOT"         | ''                                                                          | "Brussels-SR5.21.RELEASE"
        "Brussels-SR5.21"                  | ''                                                                          | "Brussels-SR5.21.RELEASE"
        "1.21"                             | ''                                                                          | "1.21.0.RELEASE"
        "1.21"                             | '1.21.0.RELEASE'                                                            | "1.21.1.RELEASE"
        "1.1.1.1.1"                        | ''                                                                          | "1.1.0.RELEASE"
        "1.1.1.1.1"                        | '1.1.1'                                                                     | "1.1.2.RELEASE"
        "Foo.1.1.1.1"                      | ''                                                                          | "Foo.1.1.1.1.RELEASE"
        "Foo.1.1.1.1-SNAPSHOT"             | ''                                                                          | "Foo.1.1.1.1.RELEASE"
    }

    @Unroll
    def "test invalid version #pomVersion"(String pomVersion) {
        setup:
        JenkinsScript script = new JenkinsScript() {
            @Override
            def sh(def input) {
                return "1.0.0.RELEASE"
            }
        }
        def pom = [version: pomVersion]
        SpringReleaseStrategy strategy = new SpringReleaseStrategy()

        when:
        strategy.releaseVersion(script, pom)

        then:
        thrown(IllegalArgumentException)

        where:
        pomVersion << ["asdf", "bar.baz"]

    }

    def "testing library params"() {
        setup:
        JenkinsScript script = new JenkinsScript()

        SpringReleaseStrategy strategy = new SpringReleaseStrategy()
        def pom = [version: '1.0.0.RELEASE-SNAPSHOT']

        when:
        strategy.releaseVersion(script, pom)

        then:
        notThrown(Exception)
    }

}
