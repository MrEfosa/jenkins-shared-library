#!/usr/bin/env groovy

def call(Map config = [:]) {
    def imageName = config.imageName
    def ec2Instance = config.ec2Instance
    def sshCredential = config.sshCredential
    def scriptFile = config.scriptFile ?: "server-cmds.sh"
    def composeFile = config.composeFile ?: "docker-compose.yaml"
    def remotePath  = config.remotePath  ?: "/home/ec2-user"

    echo "Deploying the application to EC2 server..."

    def shellCmd = "bash ./${scriptFile} ${imageName}"

    sshagent([sshCredential]) {
        sh "scp ${scriptFile} ${ec2Instance}:${remotePath}"
        sh "scp ${composeFile} ${ec2Instance}:${remotePath}"
        sh "ssh -o StrictHostKeyChecking=no ${ec2Instance} '${shellCmd}'"
    }
}