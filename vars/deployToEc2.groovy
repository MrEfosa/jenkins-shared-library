#!/usr/bin/env groovy

import com.example.Docker

def call(Map config = [:]) {
    def docker = new Docker(this)
    
    docker.deployToEc2(config)
}
