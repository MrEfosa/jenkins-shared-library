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

        sh 'git config --global user.email "jenkins@example.com"'
        sh 'git config --global user.name "jenkins"'

        sh '''
            git remote set-url origin https://$GIT_USERNAME:$GIT_PASSWORD@github.com/MrEfosa/java-maven-app.git
        '''

        sh 'git add pom.xml'

        sh """
            git diff --quiet || git commit -m "${commitMessage}"
        """

        sh "git push origin HEAD:${branch}"
    }
}