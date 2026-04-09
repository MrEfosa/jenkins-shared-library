# Jenkins Shared Library for CI/CD Automation

## Overview

This repository contains a reusable **Jenkins Shared Library** designed to simplify and standardize CI/CD pipelines for a Java Maven application.

It modularizes pipeline logic into reusable functions, enabling cleaner and more maintainable Jenkinsfiles.

---

## Features

* Modular CI/CD pipeline steps
* Reusable across multiple projects
* Supports multibranch pipelines
* Automates build, containerization, and versioning

---

## Project Structure

```text id="xk9h2p"
vars/
 ├── buildJar.groovy
 ├── buildImage.groovy
 ├── dockerLogin.groovy
 ├── dockerPush.groovy
 ├── gitCommitAndPush.groovy
 └── incrementVersion.groovy
```

---

## Available Functions

### buildJar()

Builds the Java application using Maven.

---

### buildImage()

Builds a Docker image from the application artifact.

---

### dockerLogin()

Authenticates with Docker Hub using Jenkins credentials.

---

### dockerPush()

Pushes the built Docker image to Docker Hub.

---

### incrementVersion()

Automatically increments the application version (e.g., in `pom.xml`).

---

### gitCommitAndPush()

Commits version updates and pushes them back to the repository.

* Uses dynamic branch detection via `env.BRANCH_NAME`
* Prevents CI loops using `[skip ci]` in commit messages

---

## 🔗 Usage Example

```groovy id="9c9b9g"
@Library('your-shared-library') _

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                buildJar()
            }
        }

        stage('Build Image') {
            steps {
                buildImage()
            }
        }

        stage('Push Image') {
            steps {
                dockerLogin()
                dockerPush()
            }
        }
    }
}
```

---

## Requirements

* Jenkins configured with Global Pipeline Library
* GitHub credentials stored in Jenkins
* Docker installed and accessible on Jenkins agent
* Docker Hub account for image storage

---

## Design Approach

This shared library follows:

* **Separation of concerns** → each function handles a single task
* **Reusability** → can be used across multiple pipelines
* **Scalability** → easy to extend with additional steps

---

## Benefits

* Cleaner Jenkinsfiles
* Reduced duplication
* Easier maintenance
* Standardized CI/CD workflows

---

## Author

**Onyekaozuru Tochukwu David**
