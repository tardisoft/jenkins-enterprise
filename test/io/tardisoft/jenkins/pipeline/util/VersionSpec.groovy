package io.tardisoft.jenkins.pipeline.util

import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class VersionSpec extends Specification {

    @Unroll
    def "defect fix for NPE test on list to string for #version"() {
        given:
        ArrayList<Version> versions = new ArrayList<Version>()
        versions << Version.valueOf(version)

        when:
        log.info(versions.toString())

        then:
        notThrown(NullPointerException)

        where:
        version << [
                '1.0.0',
                '1.0.1',
                '1.1.0.24',
                '1.1.0.25',
                '1.1.0.26',
                '1.1.1',
                '1.1.10',
                '1.1.11',
                '1.1.12',
                '1.1.13',
                '1.1.14',
                '1.1.2',
                '1.1.3',
                '1.1.4',
                '1.1.5',
                '1.1.6',
                '1.1.7',
                '1.1.8',
                '1.1.9',
                '1.1.X.10',
                '1.1.X.11',
                '1.1.X.12',
                '1.1.X.13',
                '1.1.X.14',
                '1.1.X.15',
                '1.1.X.16',
                '1.1.X.17',
                '1.1.X.18',
                '1.1.X.19',
                '1.1.X.20',
                '1.1.X.21',
                '1.1.X.22',
                '1.1.X.23',
                '1.1.X.4',
                '1.1.X.5',
                '1.1.X.6',
                '1.1.X.7',
                '1.1.X.8',
                '1.1.X.9']
    }

    @Unroll
    def "test Version valueOf(#inputStr)"(String inputStr, Version expectedValue) {
        when:
        Version version = Version.valueOf(inputStr)

        then:
        version == expectedValue

        where:
        inputStr                      | expectedValue
        '1.1.1'                       | new Version(major: 1, minor: 1, micro: 1, metaData: null)
        '1.1.X'                       | new Version(major: 1, minor: 1, micro: 0, metaData: null)
        '1.1.x'                       | new Version(major: 1, minor: 1, micro: 0, metaData: null)
        '1.1.1-SNAPSHOT'              | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        '1.1.X-SNAPSHOT'              | new Version(major: 1, minor: 1, micro: 0, metaData: 'SNAPSHOT')
        '1.1.1.RELEASE-SNAPSHOT'      | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        '1.1.X.RELEASE-SNAPSHOT'      | new Version(major: 1, minor: 1, micro: 0, metaData: 'RELEASE-SNAPSHOT')
        'Brussels.1.RELEASE-SNAPSHOT' | new Version(major: 'Brussels', minor: 1, metaData: 'RELEASE-SNAPSHOT')
        'Brussels.1.RELEASE-SNAPSHOT' | new Version(major: 'Brussels', minor: 1, separator: '.', metaData: 'RELEASE-SNAPSHOT')
        'Brussels.1-RELEASE-SNAPSHOT' | new Version(major: 'Brussels', minor: 1, separator: '-', metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test bad Version valueOf(#inputStr)"(String blank, String inputStr) {
        when:
        Version.valueOf(inputStr)

        then:
        thrown(IllegalArgumentException)

        where:
        blank | inputStr
        ''    | '1.X'
        ''    | '1.1SNAPSHOT'
        ''    | 'a.b'
        ''    | 'a'
    }

    @Unroll
    def "test toString #expectedStr"(Version version, String expectedStr) {
        when:
        String str = version.toString()

        then:
        str == expectedStr

        where:
        version                                                                        | expectedStr
        new Version(major: 1, minor: 1)                                                | '1.1.0'
        new Version(major: 1, minor: 1, micro: 1)                                      | '1.1.1'
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                          | '1.1.0-SNAPSHOT'
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')                | '1.1.1-SNAPSHOT'
        new Version(major: 'Brussels', minor: 1, metaData: 'SNAPSHOT')                 | 'Brussels.1-SNAPSHOT'
        new Version(major: 'Brussels', minor: 1, separator: '.', metaData: 'SNAPSHOT') | 'Brussels.1.SNAPSHOT'
        new Version(major: 'Brussels', minor: 1, separator: '-', metaData: 'SNAPSHOT') | 'Brussels.1-SNAPSHOT'
        new Version(major: 'Brussels', minor: 1)                                       | 'Brussels.1'
        new Version(major: 'Brussels', minor: 1, micro: 2)                             | 'Brussels.1'
        new Version(major: 'Brussels', minor: 1, metaData: 'RELEASE')                  | 'Brussels.1-RELEASE'
        new Version(major: 'Brussels', minor: 1, separator: '.', metaData: 'RELEASE')  | 'Brussels.1.RELEASE'
    }

    @Unroll
    def "test equals #v1 == #v2"(Version v1, Version v2) {
        expect:
        v1 == v2

        where:
        v1                                                                                      | v2
        new Version(major: "Brussels", minor: 1)                                                | new Version(major: "Brussels", minor: 1)
        new Version(major: "Brussels", minor: 2, micro: 2)                                      | new Version(major: "Brussels", minor: 2, micro: 1)
        new Version(major: "Brussels", minor: 2, metaData: 'RELEASE')                           | new Version(major: "Brussels", minor: 2, metaData: 'RELEASE')
        new Version(major: "Brussels", minor: 2, metaData: 'RELEASE')                           | new Version(major: "Brussels", minor: 2, metaData: 'RELEASE', micro: 4)
        new Version(major: "Brussels", minor: 2, separator: '.', metaData: 'RELEASE')           | new Version(major: "Brussels", minor: 2, separator: '.', metaData: 'RELEASE')
        new Version(major: "Brussels", minor: 2, micro: 1, separator: '-', metaData: 'RELEASE') | new Version(major: "Brussels", minor: 2, micro: 1, separator: '.', metaData: 'RELEASE')
        new Version(major: "Brussels", minor: 2, micro: 2, separator: '-', metaData: 'RELEASE') | new Version(major: "Brussels", minor: 2, micro: 1, separator: '.', metaData: 'RELEASE')
        new Version(major: 1, minor: 1)                                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, micro: 1,)                                              | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 1)                                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')                         | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')                          | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')                           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')                 | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')                           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test #v1 != #v2"(Version v1, Version v2) {
        given:
        log.info v1.toStringFull()
        log.info v2.toStringFull()

        expect:
        v1 != v2

        where:
        v1                                                             | v2
        new Version(major: "Brussels", minor: 1)                       | new Version(major: 2, minor: 1)
        new Version(major: "Brussels", minor: 1)                       | new Version(major: "Brussels", minor: 2)
        new Version(major: "Brussels", minor: 1, micro: 2)             | new Version(major: "Brussels", minor: 2)
        new Version(major: 1, minor: 1)                                | new Version(major: 2, minor: 1)
        new Version(major: 1, minor: 1)                                | new Version(major: 1, minor: 2)
        new Version(major: 1, minor: 1)                                | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 1, micro: 1)                      | new Version(major: 1, minor: 1, micro: 2)
        new Version(major: 1, minor: 1, micro: 1)                      | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE') | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
    }

    @Unroll
    def "test hashCode #v1.hashCode() == #v2.hashCode()"(Version v1, Version v2) {
        expect:
        v1.hashCode() == v2.hashCode()

        where:
        v1                                                                      | v2
        new Version(major: "Brussels", minor: 1)                                | new Version(major: "Brussels", minor: 1)
        new Version(major: "Brussels", minor: 1)                                | new Version(major: "Brussels", minor: 1, micro: 2)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, micro: 1,)                              | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')         | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')          | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT') | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test hashCode #v1.hashCode() != #v2.hashCode()"(Version v1, Version v2) {
        expect:
        v1.hashCode() != v2.hashCode()

        where:
        v1                                                                      | v2
        new Version(major: "Brussels", minor: 2)                                | new Version(major: "Brussels", minor: 1)
        new Version(major: "Brussels", minor: 2)                                | new Version(major: "Brussels", minor: 1, micro: 2)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2, micro: 1,)                              | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'SNAPSHOT')         | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'RELEASE')          | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'RELEASE-SNAPSHOT') | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test equals #v1 equals #v2"(Version v1, Version v2) {
        expect:
        v1.equals(v2)

        where:
        v1                                                                      | v2
        new Version(major: "Brussels", minor: 1)                                | new Version(major: "Brussels", minor: 1)
        new Version(major: "Brussels", minor: 1)                                | new Version(major: "Brussels", minor: 1, micro: 2)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, micro: 1,)                              | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')         | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')          | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT') | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test equals #v1 != #v2"(Version v1, Version v2) {
        expect:
        !v1.equals(v2)

        where:
        v1                                                                      | v2
        new Version(major: "Brussels", minor: 2)                                | new Version(major: "Brussels", minor: 1)
        new Version(major: "Brussels", minor: 2)                                | new Version(major: "Brussels", minor: 1, micro: 2)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2, micro: 1,)                              | new Version(major: 1, minor: 1, micro: 1)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2)                                         | new Version(major: 1, minor: 1)
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'SNAPSHOT')         | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'SNAPSHOT')                   | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'RELEASE')          | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE')                    | new Version(major: 1, minor: 1, metaData: 'RELEASE')
        new Version(major: 1, minor: 2, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 2, micro: 1, metaData: 'RELEASE-SNAPSHOT') | new Version(major: 1, minor: 1, micro: 1, metaData: 'RELEASE-SNAPSHOT')
        new Version(major: 1, minor: 2, metaData: 'RELEASE-SNAPSHOT')           | new Version(major: 1, minor: 1, metaData: 'RELEASE-SNAPSHOT')
    }

    @Unroll
    def "test compareTo(#v1, #v2)"(Version v1, Version v2, int compareValue) {
        when:
        int cValue = v1.compareTo(v2)
        if (cValue < 0) {
            cValue = -1
        } else if (cValue > 0) {
            cValue = 1
        } else {
            cValue = 0
        }

        then:
        cValue == compareValue

        where:
        v1                                                               | v2                                                               | compareValue
        new Version(major: "Brussels", minor: 1)                         | new Version(major: "Brussels", minor: 1)                         | 0
        new Version(major: "Brussels", minor: 1)                         | new Version(major: "Brussels", minor: 1, micro: 1)               | 0
        new Version(major: 1, minor: 1)                                  | new Version(major: 1, minor: 1)                                  | 0
        new Version(major: 1, minor: 1)                                  | new Version(major: 2, minor: 1)                                  | -1
        new Version(major: 1, minor: 1)                                  | new Version(major: 1, minor: 2)                                  | -1
        new Version(major: 1, minor: 1)                                  | new Version(major: 1, minor: 1, micro: 0)                        | 0
        new Version(major: 1, minor: 1)                                  | new Version(major: 1, minor: 1, micro: 1)                        | -1
        new Version(major: 1, minor: 1, micro: 2)                        | new Version(major: 1, minor: 1, micro: 1)                        | 1
        new Version(major: 1, minor: 1, micro: 1)                        | new Version(major: 1, minor: 1, micro: 1)                        | 0
        new Version(major: 1, minor: 1, micro: 1)                        | new Version(major: 1, minor: 1, micro: 2)                        | -1
        new Version(major: 1, minor: 1)                                  | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')            | -1
        new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')            | new Version(major: 1, minor: 1, metaData: 'SNAPSHOT')            | 0
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')  | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')  | 0
        new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')  | new Version(major: 1, minor: 1, micro: 1, metaData: 'ASNAPSHOT') | 1
        new Version(major: 1, minor: 1, micro: 1, metaData: 'ASNAPSHOT') | new Version(major: 1, minor: 1, micro: 1, metaData: 'SNAPSHOT')  | -1
    }

}