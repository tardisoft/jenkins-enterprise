package io.tardisoft.jenkins.pipeline.maven.step.deploy

import io.tardisoft.jenkins.pipeline.maven.step.AbstractMavenGithubStep

/**
 * Deploys the staging site to git hub gh-pages branch of the repository
 */
class MavenGithubPagesDeployStep extends AbstractMavenGithubStep implements Serializable {
    
    MavenGithubPagesDeployStep() {
        mavenProfiles = ['deploy-github-page']
        mavenTargets = ['site-deploy']
    }

    /**
     * Deploy release github page
     * @param script Jenkinsfile script context
     */
    def deployRelease(def script) {
        script.withCredentials([script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
            mvn(script, "site:site")
            mvn(script, "site:stage")
            List<String> args = []
            args.addAll(mavenProfiles?.collect { "-P $it" })
            args.addAll([
                    '-Dgithub.global.userName=${GIT_USER}',
                    '-Dgithub.global.password=${GIT_PASS}'
            ])
            if (version) {
                args.add("-Dgithub.site.version=${version}")
            }
            args.addAll(mavenTargets)
            mvn(script, args)
        }
    }

}
