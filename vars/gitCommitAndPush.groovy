#!/usr/bin/env groovy

def call(Map config = [:]) {

    def credentialsId = config.credentialsId ?: 'github-credentials'
    def branch = config.branch ?: env.BRANCH_NAME
    def repoUrl = config.repoUrl
    def commitMessage = config.commitMessage ?: 'ci: version bump [skip ci]'

    if (!repoUrl) {
        error("repoUrl is required")
    }

    // Ensure repo exists
    if (!fileExists('.git')) {
        git url: repoUrl, branch: branch
    }

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'GIT_USERNAME',
            passwordVariable: 'GIT_PASSWORD'
        )
    ]) {

        sh '''
            git config --global user.email "jenkins@example.com"
            git config --global user.name "jenkins"

            git add pom.xml
            git diff --quiet || git commit -m "ci: version bump [skip ci]"

            git push https://$GIT_USERNAME:$GIT_PASSWORD@github.com/MrEfosa/java-maven-app.git HEAD:master
        '''
    }