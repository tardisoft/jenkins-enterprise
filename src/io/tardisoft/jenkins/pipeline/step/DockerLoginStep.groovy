package io.tardisoft.jenkins.pipeline.step

class DockerLoginStep implements Step, Serializable {
    /**
     * Jenkins secrets ID of docker credentials
     */
    String dockerCredentialsId = 'docker_user'
    /**
     * Version of docker API to use
     */
    String dockerAPIVersion = '1.21'

    /**
     * Hostname of DTR
     */
    String dockerRegistry = 'docker.myorg.com'

    /**
     * Requires credentials added to Jenkins that match the id passed into `dockerCredentialsId`
     * @param script
     * @return
     */
    @Override
    def run(def script) {
        script.withCredentials([script.usernamePassword(credentialsId: dockerCredentialsId, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            script.sh "export DOCKER_API_VERSION=${dockerAPIVersion} && docker login -u \${DOCKER_USER} -p \${DOCKER_PASS} ${dockerRegistry}"
            script.sh "sed -i'' 's%${dockerRegistry}%https://${dockerRegistry}%g' ~/.docker/config.json"
        }
        return null
    }
}
