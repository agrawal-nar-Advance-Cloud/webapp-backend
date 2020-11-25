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

        stage('Build image') {
             steps {
                script {
                    dockerImage = docker.build ("${BACKEND_IMAGE}")
                }
             
            }
        }

        stage('Registring image') {
            steps{
                script {
                    docker.withRegistry( '', 'dockerCred' ) {
                    dockerImage.push("${git_hash}")
                    dockerImage.push("latest")
                    }
                }
            }
        }

        stage('Cluster Info') {
            steps{
                script {
                    withKubeConfig([credentialsId: 'kubernetesCred',serverUrl: "${ServerUrl}"]) {
                        sh "kubectl cluster-info"
                    }
                }
            }
        }

        stage('Write file myvalues.yaml') {
            steps{
                script {
                    writeFile file: 'helm/myvalues.yaml', text: "${BACKEND_MYVALUES}"
                }
            }
        }

        stage('Helm upgrade') {
            steps{
                script {
                    withKubeConfig([credentialsId: 'kubernetesCred',serverUrl: "${ServerUrl}"]) {
                        sh "helm upgrade backend -n api ./helm/ -f ./helm/myvalues.yaml"
                    }
                }
            }
        }


        
    }
}