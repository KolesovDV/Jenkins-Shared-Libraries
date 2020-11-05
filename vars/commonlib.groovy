def symlinkdeploy(server,phpprojectsymlink, phpprojectpath) {
     echo 'Deploying.. '
     sh "ssh root@${server} ' mv ${phpprojectsymlink} /tmp/previos_deploy && ln -s  --force ${phpprojectpath} ${phpprojectsymlink} ' "
     def httpstatus = sh (returnStdout: true, script: "curl -s -o /dev/null -w \"%{http_code}\" https://${server} " )
                println(httpstatus)
                if (httpstatus.toInteger() != 200) {
                     echo 'ROLLBACK..'
                     sh "ssh root@${server} ' rm -rf ${phpprojectsymlink}  && mv /tmp/previos_deploy ${phpprojectsymlink} ' "
                 }
}
def megretobranche(source,currentbranche,mrgbranche,tag,deletebranche) {
    echo "${source},${currentbranche},${mrgbranche},${tag},${deletebranche}"
    echo "git clone ${source} -b ${currentbranche} /tmp/${currentbranche}/${env.BUILD_NUMBER} "
    sh "git clone ${source} -b ${currentbranche} /tmp/${currentbranche}/${env.BUILD_NUMBER} "
    dir("/tmp/${currentbranche}/${env.BUILD_NUMBER}") {
        sh "git checkout ${mrgbranche}"
        echo "git branch -vv"
        sh " git merge ${currentbranche} "
        if ("${tag}" != null) {
            sh "git tag ${tag}"
            sh "git push origin ${mrgbranche} ${tag}"
        }
        else{
            sh "git push origin ${mrgbranche}"
        }
        if ("${deletebranche}" == "YES") {
            sh "git push -d origin ${currentbranche}"
        }
    }  
}

def verify(verify) {
        echo "${verify}.. "
        def userInput = input(
            id: 'userInput', message: 'This is PRODUCTION!', parameters: [
            [$class: 'BooleanParameterDefinition', defaultValue: false, description: '', name: 'Please confirm you sure to proceed']
        ])
        if(!userInput) {
            error "Build wasn't confirmed"
        }
    }