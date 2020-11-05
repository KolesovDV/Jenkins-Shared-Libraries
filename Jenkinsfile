// annotation, specifying the library’s name
@Library(['php','commonlib','tagging']) _ 

pipeline {
    options {
        // install and configure GitLab Plugin to sent build status back to GitLab
        gitLabConnection('gitlab') 
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
    }
    // This plugin allows GitLab to trigger builds in Jenkins 
    // when code is committed or merge requests are opened/updated 
    // https://github.com/jenkinsci/gitlab-plugin
    triggers {
            gitlab(
                triggerOnPush: true,
                triggerOnMergeRequest: true,
                branchFilterType: 'All',
                secretToken: "abcdefghijklmnopqrstuvwxyz0123456789ABCDEF",
                addVoteOnMergeRequest: true)
        }
    
    // parametrs to choose when build with parametrs. Examples
    parameters {
        choice(name: 'DEPLOY_FROM_BRANCH', choices: ['development', 'master'], description: 'Deploy from selected branch')
        choice(name: 'DEPLOY_TO_SELECTED_ENVIRONMENT', choices: ['dev', 'prod'], description: 'Run on specific environment')

    }
    // if defferent  stages use different agents
    agent none 
    environment {
        TAG_NAME               = sh(script: 'git tag --sort=committerdate | tail -1', , returnStdout: true).trim()       
        // parts of dnsname of server                   
        dnsrecord              = 'some name'
        aws_route53_zone       = 'zone'
        //git source with code
        php_source_build       = 'SCM link to source'
        php_project_path       = '/opt'
        //literal naming of tag
        release_name           = 'ver'
        // chat id to send messages
        telegram_chatId        = "123456789"                          
    }

    stages {
        stage('Build') {
            agent { label 'master' }
            steps {
                script {
                    updateGitlabCommitStatus name: 'Build', state: 'pending'
                    dir("${env.BUILD_NUMBER}") {
                        sh 'git version'
                        // using library named php and sent parameters
                        php.build "${dnsrecord}.${aws_route53_zone}", "${php_project_path}/deploy/${env.BUILD_NUMBER}", "${php_source_build}"
                        //archiveArtifacts artifacts: '**/', fingerprint: true , onlyIfSuccessful: true
                    }
                }
            }

            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'Build', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'Build', state: 'success'
                    }
                }
            }
        }
        stage('Test') {
            // configure stage timeout
            options {                              
                timeout(time: 30, unit: "MINUTES")
            }
            // test on stage environment
            agent { label 'stage' }
            steps {
                script {
                    updateGitlabCommitStatus name: 'test', state: 'pending'
                    dir("${env.BUILD_NUMBER}") {
                        sh 'git version'
                        php.unittest "${dnsrecord}.${aws_route53_zone}", "${php_project_path}/deploy/${env.BUILD_NUMBER}"
                        // sent message to QA team to make UI tests
                        telegramSend(message: "Please, confirm ${env.BUILD_URL}input/", chatId: "${telegram_chatId}") 
                        // qa confirm  all manual tests are passed
                        commonlib.verify "test stage"                            
                    }
                }
            }
            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'test', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'test', state: 'success'
                        
                    }
                }
            }
        }
    
        stage('release') {
            when { branch pattern: "release-\\d+", comparator: "REGEXP"}
            agent { label 'stage' }
            steps {
                script {
                    updateGitlabCommitStatus name: 'release', state: 'pending'
                    // get last tag
                    TAG_NAME = tagging.lasttaginbranche "${php_source_build}","master"
                    // get new minor release version tag
                    newtag = tagging.main "${TAG_NAME}", "${release_name}", "minor"
                    // merge current branche to master 
                    commonlib.megretobranche "${php_source_build}", "${env.BRANCH_NAME}", "master", "${newtag}", "NO" 
                    }
                }
            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'release', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'release', state: 'success'
                        
                    }
                }
            }
        }
    
         stage('hotfix') {
            when { branch pattern: "hotfix-\\d+", comparator: "REGEXP"}
            agent { label 'stage' }
            steps {
                script {
                    updateGitlabCommitStatus name: 'release', state: 'pending'
                    TAG_NAME = tagging.lasttaginbranche "${php_source_build}","master"
                    newtag = tagging.main "${TAG_NAME}", "${release_name}", "patch"
                    // merge current branche to master and delete current branch from SCM
                    commonlib.megretobranche "${php_source_build}", "${env.BRANCH_NAME}", "master", "${newtag}", "YES" 
                    }
                }
            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'hotfix', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'hotfix', state: 'success'
                        
                    }
                }
            }
        }
        // continius delivery only from master brunch when build with parameters
        stage('Delivery') {                                             
            when {                                                      
                     branch 'master'
                      not { triggeredBy 'SCMTrigger' }}  
            // to selected environment. Agents must be labled before run                   
            agent { label "${params.DEPLOY_TO_SELECTED_ENVIRONMENT}" }  
            steps {
                script {
                    updateGitlabCommitStatus name: 'Delivery', state: 'pending'
                    dir("${env.BUILD_NUMBER}") {
                        commonlib.symlinkdeploy "${dnsrecord}.${aws_route53_zone}","/opt/demo" , "${php_project_path}/deploy/${env.BUILD_NUMBER}"
                    }
                }
            }
            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'Delivery', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'Delivery', state: 'success'
                    }
                }
            }
        }
        // continius deployment only from master brunch and when triggered by SCM
        stage('Deploy') {                             
            when {                                  
                 allOf {
                      branch 'master' 
                      triggeredBy 'SCMTrigger' } }
            agent { label "production" }
            steps {
            script {
                updateGitlabCommitStatus name: 'Deploy', state: 'pending'
                commonlib.symlinkdeploy "${dnsrecord}.${aws_route53_zone}","/opt/demo" , "${php_project_path}/deploy/${env.BUILD_NUMBER}"

            }
        }
    
            post {
                failure {
                    script {
                        updateGitlabCommitStatus name: 'Deploy', state: 'failed'
                    }
                }
                success {
                    script {
                        updateGitlabCommitStatus name: 'Deploy', state: 'success'
                    }
                }
            }
        }
    }
}
    

