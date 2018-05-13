package io.tardisoft.jenkins.pipeline.maven.step.deploy

import io.tardisoft.jenkins.pipeline.util.Common


/**
 * Deploys the staging site to git hub gh-pages branch of the repository using "com.github.github:site-maven-plugin:0.13-SNAPSHOT:site "
 */
class MavenGhPagesDeployStep extends io.tardisoft.jenkins.pipeline.maven.step.AbstractMavenGithubStep implements Serializable {

    Set<String> mavenProfiles = ['asciidoc-github-page']

    String documentationModule = ''

    boolean generateMavenSite = false
    boolean generateJavadoc = false

    /**
     * Deploy release github page
     * @param script Jenkinsfile script context
     */
    def deployRelease(def script) {
        if(script.fileExists('documentation/pom.xml')) {
            documentationModule = "-pl documentation"
        } else if(script.fileExists('app/documentation')) {
            documentationModule = "-pl app"
        }

        script.withCredentials([script.usernamePassword(credentialsId: gitCredentialsId, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
            Common common = new Common(script)
            List<String> args = []
            String repo = common.getRepo()
            String org = common.getOrg()

            if(!repo || !org){
                String message =  "Failed docs: Can not generate gh-pages site due to missing repo: ${repo} or org: ${org}"
                script.echo(message)
                if(shouldFailBuild){
                    throw new RuntimeException(message)
                }
                return
            }

            if (getMavenProfiles()) {
                args.addAll(getMavenProfiles()?.collect { "-P $it" })
            }

            if(generateMavenSite){
                args.add('org.apache.maven.plugins:maven-site-plugin:site')
            }

            if(generateJavadoc){
                args.add('org.apache.maven.plugins:maven-javadoc-plugin:3.0.0:javadoc')
            }

            /*
            https://github.com/github/maven-plugins/pull/127
             */
            args.addAll([
                    documentationModule,
                    'asciidoctor:process-asciidoc',
                    'com.github.github:site-maven-plugin:0.12:site',
                    "-Dgithub.repositoryOwner=${org}",
                    "-Dgithub.repositoryName=${repo}",
                    '-Dgithub.global.userName=${GIT_USER}',
                    '-Dgithub.global.password=${GIT_PASS}',
            ])

            try {
                mvn(script, args)
            } catch (Exception e) {
                script.echo "Failed docs: ${e?.getMessage()}"
                if (shouldFailBuild) {
                    throw e
                }
            }
        }
    }

}
