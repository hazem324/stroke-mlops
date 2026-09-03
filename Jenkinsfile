pipeline {

    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '======================================'
                echo 'Checking out source code...'
                echo '======================================'

                checkout scm
            }
        }

        stage('FastAPI - SonarQube') {
            steps {
                dir('fastapi-service') {

                    script {
                        def scannerHome = tool 'sonar-scanner'

                        withSonarQubeEnv("${SONARQUBE}") {
                            sh """
                                echo "======================================"
                                echo "FastAPI SonarQube Analysis"
                                echo "======================================"

                                ${scannerHome}/bin/sonar-scanner \
                                    -Dsonar.projectKey=stroke-fastapi \
                                    -Dsonar.projectName="Stroke FastAPI" \
                                    -Dsonar.sources=. \
                                    -Dsonar.exclusions="venv/**,tests/**,__pycache__/**,.pytest_cache/**"
                            """
                        }
                    }
                }
            }
        }

        stage('FastAPI - Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Backend - SonarQube') {
            steps {
                dir('stroke_backend') {

                    withSonarQubeEnv("${SONARQUBE}") {
                        sh '''
                            echo "======================================"
                            echo "Spring Boot SonarQube Analysis"
                            echo "======================================"

                            ./mvnw sonar:sonar \
                                -Dsonar.projectKey=stroke-backend \
                                -Dsonar.projectName="Stroke Backend" \
                                -DskipTests
                        '''
                    }
                }
            }
        }

        stage('Backend - Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Frontend - SonarQube') {
            steps {
                dir('stroke_frontend') {

                    script {
                        def scannerHome = tool 'sonar-scanner'

                        withSonarQubeEnv("${SONARQUBE}") {
                            sh """
                                echo "======================================"
                                echo "Angular SonarQube Analysis"
                                echo "======================================"

                                ${scannerHome}/bin/sonar-scanner \
                                    -Dsonar.projectKey=stroke-frontend \
                                    -Dsonar.projectName="Stroke Frontend" \
                                    -Dsonar.sources=src \
                                    -Dsonar.exclusions="node_modules/**,dist/**,coverage/**"
                            """
                        }
                    }
                }
            }
        }

        stage('Frontend - Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {

        success {
            echo '''
            ==========================================
            PIPELINE SUCCESS
            ==========================================

            SonarQube Analysis:
              FastAPI   : PASSED
              Backend   : PASSED
              Frontend  : PASSED

            Quality Gates:
              FastAPI   : PASSED
              Backend   : PASSED
              Frontend  : PASSED

            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
            PIPELINE FAILED
            ==========================================

            Check the failed SonarQube stage.

            ==========================================
            '''
        }

        always {
            echo 'CI/CD pipeline finished.'
        }
    }
}