# Jenkins Enterprise Library

Base library for using testable Jenkinsfile structure

## Usage

Create a `Jenkinsfile` in your application and use the following for simple builds.

```groovy
#!groovy
@Library('jenkins-enterprise@master')
import java.lang.Object

buildApplication {
}
```

## Build Configurations

### deploySite

Set to true if your application has a `documentation` directory with adoc that should be deployed to the gh-pages branch.  

In Jenkins requires a username/password credential with your user and password under a variable `github`.  You can change the name of the credential using the optional `gitCredentialsId` in the closure.

.Example

```groovy
#!groovy
@Library('jenkins-enterprise@master')
import java.lang.Object

buildApplication {
	deploySite = true
	
	//optionally change default credentials ID to use
	gitCredentialsId = 'jenkins_github_creds_id' //defaults to `github`
}
```




