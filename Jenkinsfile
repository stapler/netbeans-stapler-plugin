// https://ci.jenkins.io/job/Stapler/job/netbeans-stapler-plugin/
pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 2, unit: 'HOURS') // lots to download
    }
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            label 'docker'
        }
    }
    stages {
        stage('main') {
            steps {
                sh 'mvn -B -ntp -Dmaven.test.failure.ignore clean verify'
            }
            post {
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts '**/target/*.nbm'
                }
            }
        }
    }
}
