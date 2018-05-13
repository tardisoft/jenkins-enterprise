package io.tardisoft.jenkins.pipeline.test

import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

@Slf4j
class JenkinsScript {
    Map scm = [:]
    Map env = [BRANCH_NAME: 'master']
    Map currentBuild = [:]
    Map poms = [:]
    Map files = [:]

    def logRotator(def input) {
        logCall("logRotator($input)")
    }

    def withEnv(def env, Closure<?> body = null) {
        logCall("withEnv($env)")
        body()
        logCall("end withEnv()")
    }

    def logCall(def msg) {
        log.debug(msg as String)
        return msg
    }

    def buildDiscarder(def input) {
        logCall("buildDiscarder($input)")
    }

    def disableConcurrentBuilds(def input) {
        logCall "disableConcurrentBuilds(${input ?: ''})"
    }

    def properties(List input) {
        logCall "properties($input)"
    }

    def timestamps(Closure closure) {
        logCall('timstamps')
        closure()
        logCall('end timstamps')
    }

    def echo(def input) {
        logCall "echo(${input})"
    }

    def node(String label, Closure closure) {
        logCall("node($label)")
        closure()
        logCall("end node($label)")
    }

    def step(def input) {
        logCall "step(${input})"
    }

    def stage(String label, Closure closure) {
        logCall "stage($label)"
        closure()
        logCall "end stage($label)"
    }


    def timeout(Map params, Closure closure) {
        logCall "timeout($params)"
        closure()
        logCall "end timeout()"
    }

    def steps(Closure closure) {
        closure()
    }

    def parallel(Map<String, Closure> steps) {
        steps.each {
            logCall("parallel step " + it.key)
            it.value()
            logCall("end parallel step " + it.key)
        }
    }

    def checkout(def input) {
        logCall "checkout(${input})"
    }

    def sh(def input) {
        logCall "sh ${input}"
    }

    def archiveArtifacts(def config) {
        logCall "archiveArtifacts($config)"
    }

    def sshagent(def input, Closure closure) {
        logCall "sshagent($input)"
        closure()
        logCall("end sshagent($input)")
    }

    String libraryResource(String path) {
        logCall "libraryResource($path)"
        InputStream stream = getClass().getResourceAsStream("/${path}")
        if (stream == null) {
            return null
        }
        return IOUtils.toString(stream)
    }

    def junit(def input) {
        logCall "junit($input)"
    }

    boolean fileExists(String input) {
        logCall "fileExists($input)"
        return files.containsKey(input)
    }

    def readFile(def input) {
        logCall "readFile($input)"
        return files[input]
    }

    def readMavenPom(def input) {
        logCall "readMavenPom($input)"
        return poms[input.file]
    }

    def withCredentials(def input, Closure closure) {
        logCall "withCredentials($input)"
        closure()
        logCall("end withCredentials($input)")
    }

    def usernamePassword(def input) {
        logCall "usernamePassword($input)"
    }

    def withSonarQubeEnv(String input, Closure closure) {
        logCall "withSonarQubeEnv($input)"
        closure()
        logCall("end withSonarQubeEnv($input)")
    }
}