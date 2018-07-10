package io.tardisoft.jenkins.pipeline.gradle.step

import com.cloudbees.groovy.cps.NonCPS
import org.apache.commons.lang.StringUtils

/**
 * Runs the quality gate process, in this case we use Sonar
 */
class QualityGateStep extends GradleStep implements Serializable {

    Boolean blocking = false
    /**
     * Name of sonar to use
     */
    String sonarQubeName = 'Sonar'
    /**
     * Sonar token
     */
    String sonarQubeToken = 'XXXX'
    /**
     * OAuth token
     */
    String sonarGithubOauthToken = 'YYYY'
    /**
     * Url to sonar
     */
    String sonarBaseUrl = 'http://sonarqube.mycorp.com'
    /**
     * Github API endpoint
     * GHE would be something like https://github.myorg.com/api/v3
     */
    String sonarGithubEndpoint = 'https://api.github.com'

    /**
     * {@inheritDoc}
     */
    @Override
    def run(Object script) {
        def PULL_REQUEST = script.env.CHANGE_ID ?: null
        List<String> args = []
        args.add('org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar')
        args.add("-Dsonar.login=$sonarQubeToken")

        String repo = getOrgRepo(script)
        if (StringUtils.isNotBlank(repo)) {
            args.add("-Dsonar.github.repository=${repo}")
        }

        if (PULL_REQUEST != null) {
            args.add("-Dsonar.github.pullRequest=${PULL_REQUEST}")
            args.add("-Dsonar.analysis.mode=preview")
        }

        args.add("-Dsonar.github.oauth=$sonarGithubOauthToken")
        args.add("-Dsonar.host.url=$sonarBaseUrl")
        args.add("-Dsonar.github.endpoint=$sonarGithubEndpoint")

        try {
            script.withSonarQubeEnv(sonarQubeName) {
                gradle(script, args)
            }
        } catch (Exception e) {
            if (blocking) {
                throw e
            }
            script.echo "SONAR: Error - could not run sonar due to ${e?.getMessage()}"
            script.echo "SONAR: Will continue and not block builds."
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonCPS
    private static String getOrgRepo(def script) {
        if (!script.scm.userRemoteConfigs) {
            return null
        }
        (script.scm.userRemoteConfigs?.first()?.url =~ /^https:\/\/(.*)\/(.*)\/(.*)\.git$/)?.collect { m, h, o, r -> [o, r] }?.first()?.join('/')
    }
}
