////// Semantic versioning. https://semver.org/
//last_tag          - tag which number we need to increment
//release_name      - literal naming of tag. Ex. "ver","release" etc
//release_version   - wchich one version to increment Ex. "major", "minor","patch"
// Example main("ver6.143.2","ver","minor") result : ver6.144.0

def main (last_tag, release_name, release_version) {
    major   = release_name
    minor   = ver(last_tag, major)
    patch   = ver(last_tag, minor)

    if (release_version == 'minor') {
        release = count(last_tag, ver(last_tag, major))
        return  release + '.0'
    }
                            else if (release_version == 'patch') {
        release = count(last_tag, patch)
        return release
                            }
                            else if (release_version == 'major') {
        release = count(last_tag, major)
        return release + '.0.0'
                            }
}

def ver(lasttag, release) {
    indx = release.length()
    result = release + lasttag[indx]
    while (lasttag[indx] != '.') {
        indx++
        result = result + lasttag[indx]
    }
    return result
}

def count(last_tag, release) {
    if (release.length() <= (last_tag.length())  ) {
        iter = 1
        int newver = last_tag[release.length()].toInteger()
        try {
            while (((last_tag[release.length() + iter])) != '.') {
                newver = (newver.toString() + last_tag[release.length() + iter]).toInteger()
                iter++
            }
                               } catch (Exception ex) {
        }
        newver++
        String  releaseversion = (release + newver.toString())
        return releaseversion
    }
}

def lasttaginbranche(resource,branchname){
    sh "git clone ${resource} -b ${branchname} /tmp/${branchname}/${env.BUILD_NUMBER}"
    dir("/tmp/${branchname}/${env.BUILD_NUMBER}") {
        result = sh(script: 'git tag --sort=committerdate | tail -1', , returnStdout: true).trim()
        return result

    }
}
