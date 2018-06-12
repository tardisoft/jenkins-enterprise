package io.tardisoft.jenkins.pipeline.maven.step.notify

import io.tardisoft.jenkins.pipeline.util.Common

class DatadogNotifyExternalStep extends AbstractNotifyExternalStep {

    @Override
    def run(def script) {
        if (new Common(script).isReleaseBranch(["master"])) {
            notifyExternal(script)
        }
    }

    @Override
    def notifyExternal(def script) {
        script.withCredentials([script.usernamePassword(credentialsId: "DD_API_KEYS", usernameVariable: 'DD_API_USER', passwordVariable: 'DD_API_KEY')]) {
            def common = new Common(script)
            script.sh("""curl  -X POST -H "Content-type: application/json" \\
    -d '{
          "title": "Application Build Event - ${common.getRepo()}",
          "text": "Application was built.",
          "priority": "normal",
          "tags": ["org:${common.getOrg()}", "repo:${common.getRepo()}", "environment:build", "branch:${common.getBranch()}"],
          "alert_type": "info"
    }' \\
    'https://api.datadoghq.com/api/v1/events?api_key=\$DD_API_KEY'
    """)
        }
    }
}