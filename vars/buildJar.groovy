#!/usr/bin/env groovy
def call(){
    echo "building the application for $BRANCH_NAME branch"
    sh 'mvn package'
}