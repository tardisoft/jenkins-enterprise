package io.tardisoft.jenkins.pipeline.util

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification
import spock.lang.Unroll

class CommonSpec extends Specification {

    @Unroll("test isReleaseBranch branch: testBranch releaseBranches: #releaseBranches  isReleaseBranch: #isReleaseBranch")
    def "test isReleaseBranch"(def releaseBranches, def isReleaseBranch) {
        setup:
        JenkinsScript jenkins = new JenkinsScript()
        jenkins.env.BRANCH_NAME = 'testBranch'

        Common common = new Common(jenkins)

        expect:
        common.isReleaseBranch(releaseBranches) == isReleaseBranch

        where:
        releaseBranches         | isReleaseBranch
        []                      | false
        ['hello']               | false
        ['hello.*']             | false
        ['hello', 'testBranch'] | true
        ['hello', 'test.*']     | true
    }

    @Unroll("test isReleaseBranch branch: testBranch releaseBranches: #releaseBranches  isReleaseBranch: #isReleaseBranch")
    def "test isReleaseBranch null env"(def releaseBranches, def isReleaseBranch) {
        setup:
        JenkinsScript jenkins = new JenkinsScript() {
            @Override
            def sh(Object script) {
                super.sh(script)
                return 'testBranch'
            }
        }
        jenkins.env.BRANCH_NAME = null

        Common common = new Common(jenkins)

        expect:
        common.isReleaseBranch(releaseBranches) == isReleaseBranch

        where:
        releaseBranches         | isReleaseBranch
        []                      | false
        ['hello']               | false
        ['hello.*']             | false
        ['hello', 'testBranch'] | true
        ['hello', 'test.*']     | true
    }

    def "test getBranch no git repo"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript() {
            @Override
            def sh(Object script) {
                throw new RuntimeException("no git")
            }
        }
        jenkins.env.BRANCH_NAME = null

        Common common = new Common(jenkins)

        expect:
        common.getBranch() == null
    }

    def "test getBranch"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript()
        jenkins.env.BRANCH_NAME = 'testBranch'

        Common common = new Common(jenkins)

        expect:
        common.getBranch() == 'testBranch'
    }

    @Unroll("test isPullRequest branch: #branch isPullRequest: #isPullRequest")
    def "test isPullRequest"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript()
        jenkins.env.BRANCH_NAME = 'testBranch'

        Common common = new Common(jenkins)

        when:
        jenkins.env.BRANCH_NAME = branch

        then:
        common.isPullRequest() == isPullRequest

        where:
        branch  | isPullRequest
        null    | false
        ''      | false
        'hello' | false
        'PR-'   | false
        'PR-1'  | true
    }

    @Unroll("test getOrgRepo #expected")
    def "test getOrgRepo"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript()

        Common common = new Common(jenkins)

        when:
        jenkins.scm.userRemoteConfigs = scm

        then:
        common.getOrgRepo() == expected

        where:
        scm                                                                                               | expected
        [[url: "https://www.github.com/test/hello.git"]]                                                  | "test/hello"
        [[]]                                                                                              | ""
        [[url: "https://www.github.com/hello/test.git"]]                                                  | "hello/test"
        [[url: "https://www.github.com/myorg/test.git"], [url: "https://www.github.com/hello/world.git"]] | "myorg/test"
    }

    @Unroll("test getOrg #expected")
    def "test getOrg"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript()

        Common common = new Common(jenkins)

        when:
        jenkins.scm.userRemoteConfigs = scm

        then:
        common.getOrg() == expected

        where:
        scm                                                                                               | expected
        [[url: "https://www.github.com/test/hello.git"]]                                                  | "test"
        [[]]                                                                                              | ""
        [[url: "https://www.github.com/hello/test.git"]]                                                  | "hello"
        [[url: "https://www.github.com/myorg/test.git"], [url: "https://www.github.com/hello/world.git"]] | "myorg"
    }

    @Unroll("test getRepo #expected")
    def "test getRepo"() {
        setup:
        JenkinsScript jenkins = new JenkinsScript()

        Common common = new Common(jenkins)

        when:
        jenkins.scm.userRemoteConfigs = scm

        then:
        common.getRepo() == expected

        where:
        scm                                                                                               | expected
        [[url: "https://www.github.com/test/hello.git"]]                                                  | "hello"
        [[]]                                                                                              | ""
        [[url: "https://www.github.com/hello/test.git"]]                                                  | "test"
        [[url: "https://www.github.com/myorg/test.git"], [url: "https://www.github.com/hello/world.git"]] | "test"
    }
}

