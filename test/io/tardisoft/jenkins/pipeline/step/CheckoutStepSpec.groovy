package io.tardisoft.jenkins.pipeline.step

import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class CheckoutStepSpec extends Specification {

    CheckoutStep checkoutStep = new CheckoutStep()
    JenkinsScript jenkins = Spy(JenkinsScript)

    def setup() {
        jenkins.scm.branches = 'testbranch'
        jenkins.env.BRANCH_NAME = jenkins.scm.branches
        jenkins.scm.extensions = [[current: 'extensions']]
        jenkins.scm.userRemoteConfigs = ['remoteConfigs']
    }

    def "test createConfig"() {
        when:
        def config = checkoutStep.createConfig(jenkins)

        then:
        config == [
                $class           : 'GitSCM',
                branches         : jenkins.scm.branches,
                extensions       : jenkins.scm.extensions + [[$class: 'CloneOption', noTags: false], [$class: 'CleanCheckout'], [$class: 'LocalBranch', localBranch: "${jenkins.env.BRANCH_NAME}"]],
                userRemoteConfigs: jenkins.scm.userRemoteConfigs
        ]
    }

    def "test run"() {
        setup:
        def testName = 'testName'
        def testEmail = "test@emal.net"
        checkoutStep.GIT_COMMITTER_NAME = 'testName'
        checkoutStep.GIT_COMMITTER_EMAIL = "test@emal.net"

        when:
        checkoutStep.run(jenkins)

        then:
        1 * jenkins.checkout(_)
        1 * jenkins.sh("git config user.name ${testName}")
        1 * jenkins.sh("git config user.email ${testEmail}")
    }
}
