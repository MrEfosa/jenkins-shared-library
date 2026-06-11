#!/usr/bin/env groovy

package com.example

class Docker implements Serializable {
    def script

    Docker(script) {
        this.script = script
    }
    def incrementVersion() {
        script.echo 'incrementing app version...'

        script.sh '''
            mvn build-helper:parse-version versions:set \
            -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} \
            versions:commit
        '''
        def version = script.sh(
            script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
            returnStdout: true
        ).trim()
        script.env.IMAGE_NAME = "${version}-${script.env.BUILD_NUMBER}"
        return version
    }
    def mavenBuild() {
        script.echo "Compiling and packaging the Java application..."
        script.sh "mvn clean package"
    }

    def buildDockerImage(String imageName) {
        script.echo "building the docker image..."
        script.sh "docker build -t ${imageName} ."
    }

    def dockerLogin() {
        script.withCredentials([script.usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
            script.sh "echo \$PASS | docker login -u \$USER --password-stdin"
        }
    }

    def dockerPush(String imageName) {
        script.sh "docker push ${imageName}"
    }

    def deployToEc2(Map config = [:]) {
        script.echo "deploying the docker image to EC2..."
        
        def imageName   = config.imageName
        def ec2User     = config.ec2User ?: "ec2-user"
        def ec2Ip       = config.ec2Ip
        def ec2Instance = "${ec2User}@${ec2Ip}"
        
        script.sshagent(['ec2-ssh-credentials']) {
            script.sh """
                ssh -o StrictHostKeyChecking=no ${ec2Instance} 'docker pull ${imageName} && docker run -d ${imageName}'
            """
        }
    }

    def gitCommitAndPush(Map config = [:]){
        def credentialsId = config.credentialsId ?: 'github-credentials'
        def branch        = config.branch ?: env.BRANCH_NAME
        def repoUrl       = config.repoUrl
        def commitMessage = config.commitMessage ?: 'ci: version bump [skip ci]'

        if (!repoUrl) {
            error("repoUrl is required")
        }

        def cleanUrl = repoUrl.replace("https://", "").replace("http://", "")

        script.withCredentials([
            script.usernamePassword(
                credentialsId: credentialsId,
                usernameVariable: 'USER',
                passwordVariable: 'PASS'
            )
        ]) {
            // Configure local commit identity
            script.sh 'git config --global user.email "jenkins@example.com"'
            script.sh 'git config --global user.name "jenkins"'
            script.sh "git remote set-url origin https://\$USER:\$PASS@${cleanUrl}"
            
            // Handle the staging, fallback logic for clean empty-state commits, and pushing
            script.sh 'git add pom.xml'
            script.sh "git commit -m \"${commitMessage}\" || echo 'No changes to commit'"
            script.sh "git push origin HEAD:${branch}"
        }
    }
}
