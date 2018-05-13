package io.tardisoft.jenkins.pipeline.maven.step.deploy

import io.tardisoft.jenkins.pipeline.maven.step.deploy.MavenGhPagesDeployStep
import io.tardisoft.jenkins.pipeline.test.JenkinsScript
import spock.lang.Specification

class MavenGhPagesDeployStepSpec extends Specification {

    def "test run"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)
        }
    }

    def "test run with maven site"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep(generateMavenSite: true)
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page org.apache.maven.plugins:maven-site-plugin:site asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)
        }
    }

    def "test run with maven site and javadoc"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep(generateMavenSite: true, generateJavadoc: true)
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page org.apache.maven.plugins:maven-site-plugin:site org.apache.maven.plugins:maven-javadoc-plugin:3.0.0:javadoc asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)
        }
    }

    def "test run missing org/repo not fail build"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: '']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        notThrown(RuntimeException)
        verifyAll {
            1 * script.echo(_)
            0 * script.sh(_)
        }
    }

    def "test run missing org/repo fail build"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: '']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']
        deployStep.shouldFailBuild = true

        when:
        deployStep.run(script)

        then:
        thrown(RuntimeException)
        verifyAll {
            1 * script.echo(_)
            0 * script.sh(_)
        }
    }

    def "test run on wrong branch"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'PR-0'
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            0 * script.sh(_)
        }
    }

    def "test run app module"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> false
            1 * script.fileExists("app/documentation") >> true
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page -pl app asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)

        }
    }

    def "test run with maven site app module"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep(generateMavenSite: true)
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> false
            1 * script.fileExists("app/documentation") >> true
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page org.apache.maven.plugins:maven-site-plugin:site -pl app asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)
        }
    }


    def "test run documentation module"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> true
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page -pl documentation asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)

        }
    }

    def "test run with maven site documentation module"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: 'https://www.github.com/myorg/my-service.git']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep(generateMavenSite: true)
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> true
            1 * script.sh('export MAVEN_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true" && mvn -nsu --batch-mode -e -V -Dmaven.repo.local=$PWD/.repository -P asciidoc-github-page org.apache.maven.plugins:maven-site-plugin:site -pl documentation asciidoctor:process-asciidoc com.github.github:site-maven-plugin:0.12:site -Dgithub.repositoryOwner=myorg -Dgithub.repositoryName=my-service -Dgithub.global.userName=${GIT_USER} -Dgithub.global.password=${GIT_PASS}')
            0 * script.sh(_)
        }
    }

    def "test library run missing org/repo not fail build"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: '']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        notThrown(RuntimeException)
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> true
            1 * script.echo(_)
            0 * script.sh(_)
        }
    }

    def "test library run missing org/repo fail build"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'master'
        script.scm.userRemoteConfigs = [[url: '']]
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']
        deployStep.shouldFailBuild = true

        when:
        deployStep.run(script)

        then:
        thrown(RuntimeException)
        verifyAll {
            1 * script.fileExists("documentation/pom.xml") >> true
            1 * script.echo(_)
            0 * script.sh(_)
        }
    }

    def "test app run on wrong branch"() {
        given:
        JenkinsScript script = Spy(JenkinsScript)
        script.env.BRANCH_NAME = 'PR-0'
        MavenGhPagesDeployStep deployStep = new MavenGhPagesDeployStep()
        deployStep.releaseBranches = ['master']

        when:
        deployStep.run(script)

        then:
        verifyAll {
            0 * script.sh(_)
        }
    }
}
