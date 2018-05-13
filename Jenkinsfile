#!/usr/bin/env groovy

def version = ""

pipeline {
    agent any
    tools {
        maven 'Maven 3.3.9'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    version = readMavenPom(file: "pom.xml")?.version

                }
            }
        }

        stage('Build') {
            steps {
                script {
                    currentBuild.displayName = version
                }
                sh 'mvn -nsu --batch-mode -e -V clean install'
            }
            post {
                always {
                    script {
                        sh "touch target/surefire-reports/*.xml"
                    }
                    archiveArtifacts artifacts: 'target/*.jar'
                    junit testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Publish') {
            when {
                branch "master"
            }
            steps {
                withMaven(maven: 'Maven 3.3.9', mavenSettingsConfig: 'global') {
                    sh "mvn -nsu --batch-mode -e -V deploy -Dmaven.test.skip=true"
                }
            }
        }
    }
}