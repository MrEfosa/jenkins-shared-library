#!/usr/bin/env groovy

import com.example.Docker

def call(Map config = [:]) {
    // 1. Instantiate your Docker class, passing 'this' (the Jenkins context)
    def docker = new Docker(this)
    
    docker.deployToEc2(config)
}