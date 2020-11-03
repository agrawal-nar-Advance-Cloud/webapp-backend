pipeline {
    agent any

    stages {

       stage('Git Clone') {
          steps {
                checkout scm
            }
        }

       stage('Get last git commit') {
          steps {
                script {
                git_hash = sh(returnStdout: true, script: "git rev-parse HEAD").trim()
               }
            }
        }

        stage('Build JAR') {
            steps {
                sh "mvn clean install"
            }
        }

        stage('Building image') {
            steps{
            dockerImage = docker.build ("${BACKEND_IMAGE}")
            }
        }

        stage('Registring image') {
            steps{
                docker.withRegistry( '', 'dockerCred' ) {
                    dockerImage.push("${git_hash}")
                    dockerImage.push("latest")
                }
            }
        }
    }


}