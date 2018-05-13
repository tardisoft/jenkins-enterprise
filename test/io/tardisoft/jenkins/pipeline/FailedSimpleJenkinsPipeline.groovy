package io.tardisoft.jenkins.pipeline


class FailedSimpleJenkinsPipeline extends AbstractJenkinsPipeline {

    String buildAndTestResult = "SUCCESS"

    FailedSimpleJenkinsPipeline(buildAndTestResult) {
        this.buildAndTestResult = buildAndTestResult
    }

    @Override
    void buildAndTest(Object script) {
        script.currentBuild = [result: buildAndTestResult]
    }

    @Override
    void qualityGate(Object script) {

    }

    @Override
    void publish(Object script) {

    }

    @Override
    void deploy(Object script) {

    }

    @Override
    void site(Object script) {

    }

    @Override
    void cleanup(Object script) {

    }

    @Override
    void notifyExternal(Object script) {

    }
}
