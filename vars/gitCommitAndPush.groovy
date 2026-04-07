#!/usr/bin/env groovy

def call(Map config = [:]) {

    def credentialsId = config.credentialsId ?: 'github-credentials'
    def branch = config.branch ?: 'jenkins-jobs'
    def repoUrl = config.repoUrl
    def commitMessage = config.commitMessage ?: 'ci: version bump [skip ci]'

    if (!repoUrl) {
        error("repoUrl is required")
    }

    withCredentials([
        usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'USER',
            passwordVariable: 'PASS'
        )
    ]) {

        sh 'git config --global user.email "jenkins@example.com"'
        sh 'git config --global user.name "jenkins"'

        sh "git remote set-url origin https://${USER}:${PASS}@${repoUrl}"

        sh 'git add pom.xml'
        sh "git commit -m \"${commitMessage}\" || echo 'No changes to commit'"
        sh "git push origin HEAD:${branch}"
    }
}