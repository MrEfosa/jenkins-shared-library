# Jenkins Shared Library for Java/Maven CI/CD Automation

## Overview
This repository contains an enterprise-ready, object-oriented **Jenkins Shared Library** designed to standardize, modularize, and accelerate CI/CD workflows for Java Maven applications. 

By abstracting complex pipeline mechanics away from individual application repositories, this library enables developers to maintain minimal, declarative `Jenkinsfiles` while platform engineers retain centralized control over the build, testing, and deployment execution logic.

---

## Project Architecture & Structure
This library separates its pipeline steps into two clear areas: 
* **Global Shortcuts (`vars/`)** – Clean, one-word pipeline commands that developers can easily call in their Jenkinsfile.
* **The Core Engine (`src/`)** – The background helper file where the actual complex commands (like Maven and Docker shell operations) are securely stored and managed.

```text
.
├── src/
│   └── com/
│       └── example/
│           └── Docker.groovy         # The core engine containing all lifecycle methods
└── vars/
    ├── buildJar.groovy               # Compiles and packages Java source via Maven
    ├── buildImage.groovy             # Constructs Docker image using dynamic tagging
    ├── dockerLogin.groovy            # Authenticates securely against Docker Hub
    ├── dockerPush.groovy             # Uploads tagged image layers to registry
    ├── incrementVersion.groovy       # Automates semantic patch version bumps
    ├── gitCommitAndPush.groovy       # Synchronizes modified pom.xml back to GitHub
    └── deployToEc2.groovy            # Provisions remote runtime deployment over SSH
---

## Key Features

* **Centralized Code Logic:** Keeps all messy shell scripts inside a single hidden file (`src/`), making individual pipeline steps clean and easy to read.
* **Smart Version Bumping:** Uses automated Maven tools to automatically increase the app version number (e.g., `1.4.2` to `1.4.3`) without any human typing.
* **Tight Security Controls:** Keeps passwords hidden from console logs and streams them safely through background memory directly into Docker Hub.
* **Protected Server Access:** Uses secure, temporary digital keys to log onto AWS EC2 instances, deleting them immediately after deployment so nothing is left behind on disk.
* **Infinite Loop Interception:** Automatically adds a `[skip ci]` tag to automated code updates so Jenkins knows not to trigger itself over and over again in a loop.

---

## Available Pipeline Commands & Options

### `incrementVersion()`
Automatically bumps the last digit of your app's version number in the `pom.xml` file (e.g., `1.4.2` to `1.4.3`) and saves this new number so later steps can use it.

### `buildJar()`
Runs the standard Maven command to compile your Java source code into a runnable package.

### `buildImage(Map config)`
Converts your compiled Java code into a Docker container image.
* `imageName` (Required): The name and version tag you want to give to your Docker image.

### `dockerLogin(Map config)`
Logs into your Docker Hub account securely.
* `credentialsId` (Optional): The secret key name saved inside Jenkins. Defaults to `'docker-hub-repo'`.

### `gitCommitAndPush(Map config)`
Saves the updated version number back to your GitHub repository automatically.
* `repoUrl` (Required): Your project's full GitHub address.
* `branch` (Optional): The branch you want to push to. Defaults to your active branch.
* `credentialsId` (Optional): Your saved GitHub access token name inside Jenkins. Defaults to `'github-credentials'`.
* `commitMessage` (Optional): The text message for the Git update. Defaults to `'ci: version bump [skip ci]'`.

### `deployToEc2(Map config)`
Connects to your AWS cloud server and launches your updated Docker image.
* `imageName` (Required): The full name of the Docker Hub image to download and run.
* `ec2Ip` (Required): The network IP address of your AWS EC2 virtual machine.
* `ec2User` (Optional): The Linux profile name used to log into AWS. Defaults to `'ec2-user'`.

---

## Live Implementation Example

```groovy id="9c9b9g"
@Library('your-shared-library') _

pipeline {
    agent any

    stages {
        stage('Semantic Versioning') {
            steps {
                // Automates patch increment and outputs back to env.IMAGE_NAME
                incrementVersion() 
            }
        }

        stage('Compile binary') {
            steps {
                buildJar()
            }
        }

        stage('Containerize Asset') {
            steps {
                buildImage(imageName: "sirdavidchris/java-app:${env.IMAGE_NAME}")
            }
        }

        stage('Publish Artifacts') {
            steps {
                dockerLogin(credentialsId: 'docker-hub-repo')
                dockerPush(imageName: "sirdavidchris/java-app:${env.IMAGE_NAME}")
            }
        }

        stage('Synchronize Upstream Git') {
            steps {
                gitCommitAndPush(
                    repoUrl: '[https://github.com/MrEfosa/java-maven-app.git](https://github.com/MrEfosa/java-maven-app.git)',
                    branch: 'master',
                    credentialsId: 'github-credentials'
                )
            }
        }

        stage('Execute Cloud Deployment') {
            steps {
                deployToEc2(
                    imageName: "sirdavidchris/java-app:${env.IMAGE_NAME}",
                    ec2Ip: '54.165.131.131',
                    ec2User: 'ubuntu'
                )
            }
        }
    }
}
```
---

## Requirements and System Dependencies

* **Jenkins Setup:** Jenkins (Version 2.x or higher) must have the "Pipeline: Shared Groovy Libraries" plugin installed.
* **Security Credentials:** You must save your login secrets inside Jenkins' secure credential vault:
  * Username and Password keys for **Docker Hub** and **GitHub**.
  * An SSH Private Key (your `.pem` file) for your **AWS EC2 server**.
* **Installed Tools:** The server running your Jenkins builds must have **Git**, **Docker**, and **Java/Maven** pre-installed and ready to use.

---

## Author

* **Onyekaozuru Tochukwu David** - *DevOps & Cloud Systems Engineer*
