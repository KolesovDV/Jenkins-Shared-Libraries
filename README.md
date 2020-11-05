# Jenkins shared libraries, Semantic versioning, ci cd cdl cdp
### ***Jenkins shared libraries***
It is useful to share parts of Pipelines between various projects to reduce redundancies and keep code "DRY"
The way how to use Jenkins shared  libraries is good written in [documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)
### ***[Semantic versioning](https://semver.org/)***
 Code is [here](https://github.com/KolesovDV/Jenkins-Shared-Libraries/blob/master/vars/tagging.groovy)
Given a version number MAJOR.MINOR.PATCH, increment the:
MAJOR version when you make incompatible API changes,
MINOR version when you add functionality in a backwards compatible manner, and
PATCH version when you make backwards compatible bug fixes.
Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.


### ***Continuous integration vs. continuous delivery vs. continuous deployment***
- **Continuous integration** puts a great emphasis on testing automation to check that the application is not broken whenever new commits are integrated into the main branch

- **Continuous delivery** is an extension of continuous integration to make sure that you can release new changes to your customers quickly in a sustainable way. This means that on top of having automated your testing, you also have automated your release process and you can deploy your application at any point of time by clicking on a button
```
     stage('Delivery') {                                             
          when {                                                      
            branch 'master'
            not { triggeredBy 'SCMTrigger' }}  

```
- **Continuous deployment** goes one step further than continuous delivery. With this practice, every change that passes all stages of your production pipeline is released to your customers. There's no human intervention, and only a failed test will prevent a new change to be deployed to production.
```
stage('Deploy') {                             
          when {                                  
             allOf {
              branch 'master' 
              triggeredBy 'SCMTrigger' } }
```
