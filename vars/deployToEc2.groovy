#!/usr/bin/env groovy

def call(Map config = [:]) {
    def imageName = config.imageName
    def ec2Instance = config.ec2Instance
    def sshCredential = config.sshCredential
    def scriptFile = config.scriptFile ?: "server-cmds.sh"
    def composeFile = config.composeFile ?: "docker-compose.yaml"
    def remotePath  = config.remotePath  ?: "/home/ec2-user"

    echo "Deploying the application to EC2 server..."

    def shellCmd = "bash ${remotePath}/${scriptFile} ${imageName}"

    sshagent([sshCredential]) {
        sh """
        mkdir -p ~/.ssh
        ssh-keyscan -H ${ec2Instance.split('@')[1]} >> ~/.ssh/known_hosts

        scp ${scriptFile} ${ec2Instance}:${remotePath}
        scp ${composeFile} ${ec2Instance}:${remotePath}
        ssh ${ec2Instance} '${shellCmd}'
        """
    }
}