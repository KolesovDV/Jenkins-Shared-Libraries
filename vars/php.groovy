def build(server, phpprojectpath, phpsourcebuild) {
    echo 'Build..'
    sh "ssh root@${server} 'mkdir -p  ${phpprojectpath} && cd ${phpprojectpath}  && git clone ${phpsourcebuild} . '"
    sh "ssh root@${server} ' cd ${phpprojectpath} && COMPOSER_MEMORY_LIMIT=-1 /usr/local/bin/composer update   ' "
    sh "ssh root@${server} ' cd ${phpprojectpath} && COMPOSER_MEMORY_LIMIT=-1 /usr/local/bin/composer install   ' "
    sh "ssh root@${server} ' cd ${phpprojectpath} && /usr/local/bin/composer update tgalopin/html-sanitizer && /usr/local/bin/composer update ' "
    //sh "ssh root@${server} ' cd ${phpprojectpath} &&  /usr/local/bin/composer update erusev/parsedown && /usr/local/bin/composer update '"
}

def unittest(server,  phpprojectpath) {
    sh "ssh root@${server} 'cd ${phpprojectpath} && bin/phpunit '"
}
def symlinkdeploy(server,phpprojectsymlink, phpprojectpath) {
     echo 'Deploying.. '
     sh "ssh root@${server} ' mv ${phpprojectsymlink} /tmp/previos_deploy && ln -s  --force ${phpprojectpath} ${phpprojectsymlink} ' "
     def httpstatus = sh (returnStdout: true, script: "curl -s -o /dev/null -w \"%{http_code}\" https://${server} " )
                println(httpstatus)
                if (httpstatus.toInteger() != 200) {
                     echo 'ROLLBACK..'
                     sh "ssh root@${server} ' mv ${phpprojectpath}/previos_deploy ${phpprojectsymlink} "
                 }
}
