pipeline {

    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

    tools {
        sonarRunner 'sonar-scanner'
    }

    stages {

        // =========================================================
        // 1. CHECKOUT
        // =========================================================

        stage('Checkout') {
            steps {
                echo '======================================'
                echo 'Checking out source code...'
                echo '======================================'

                checkout scm
            }
        }


        // =========================================================
        // 2. FASTAPI - SONARQUBE
        // =========================================================

        stage('FastAPI - SonarQube') {
            steps {

                dir('fastapi-service') {

                    withSonarQubeEnv("${SONARQUBE}") {

                        sh '''
                            echo "======================================"
                            echo "FastAPI SonarQube Analysis"
                            echo "======================================"

                            sonar-scanner \
                                -Dsonar.projectKey=stroke-fastapi \
                                -Dsonar.projectName="Stroke FastAPI" \
                                -Dsonar.sources=. \
                                -Dsonar.exclusions="venv/**,tests/**,__pycache__/**,.pytest_cache/**"
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 3. FASTAPI - QUALITY GATE
        // =========================================================

        stage('FastAPI - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 4. BACKEND - SONARQUBE
        // =========================================================

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


        // =========================================================
        // 5. BACKEND - QUALITY GATE
        // =========================================================

        stage('Backend - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 6. FRONTEND - SONARQUBE
        // =========================================================

        stage('Frontend - SonarQube') {
            steps {

                dir('stroke_frontend') {

                    withSonarQubeEnv("${SONARQUBE}") {

                        sh '''
                            echo "======================================"
                            echo "Angular SonarQube Analysis"
                            echo "======================================"

                            sonar-scanner \
                                -Dsonar.projectKey=stroke-frontend \
                                -Dsonar.projectName="Stroke Frontend" \
                                -Dsonar.sources=src \
                                -Dsonar.exclusions="node_modules/**,dist/**,coverage/**"
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 7. FRONTEND - QUALITY GATE
        // =========================================================

        stage('Frontend - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }


    // =============================================================
    // POST
    // =============================================================

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