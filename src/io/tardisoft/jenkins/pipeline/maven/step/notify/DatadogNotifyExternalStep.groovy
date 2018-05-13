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
        script.sh("""curl  -X POST -H "Content-type: application/json" \\
-d '{
      "title": "Did you hear the news today?",
      "text": "Oh boy!",
      "priority": "normal",
      "tags": ["environment:build","branch:${new Common(script).getBranch()}"],
      "alert_type": "info"
}' \\
'https://api.datadoghq.com/api/v1/events?api_key=<YOUR_API_KEY>'
""")
    }
}