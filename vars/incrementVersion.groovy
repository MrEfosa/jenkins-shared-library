#!/usr/bin/env groovy

def call() {
    echo 'incrementing app version...'

    sh '''
        mvn build-helper:parse-version versions:set \
        -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} \
        versions:commit
    '''

    def version = sh(
        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
        returnStdout: true
    ).trim()

    env.IMAGE_NAME = "${version}-${env.BUILD_NUMBER}"

    return version
}

