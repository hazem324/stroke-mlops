pipeline {

    agent any

    environment {
        // SonarQube server configured in Jenkins
        SONARQUBE = 'SonarQube'

        // Docker images
        FASTAPI_IMAGE = 'hazem231/stroke-fastapi:latest'
        BACKEND_IMAGE = 'hazem231/stroke-backend:latest'
        FRONTEND_IMAGE = 'hazem231/stroke-frontend:latest'
    }

    stages {

        // =========================================================
        // 1. CHECKOUT
        // =========================================================

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }


        // =========================================================
        // 2. FASTAPI TEST
        // =========================================================

        stage('FastAPI - Test') {
            steps {
                dir('fastapi-service') {

                    sh '''
                        echo "======================================"
                        echo "FastAPI Tests"
                        echo "======================================"

                        python3 --version

                        python3 -m venv venv

                        . venv/bin/activate

                        pip install --upgrade pip

                        pip install -r requirements.txt

                        pip install pytest pytest-cov

                        pytest \
                            --cov=. \
                            --cov-report=xml:coverage.xml \
                            --cov-report=term \
                            -v
                    '''
                }
            }

            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'fastapi-service/test-results.xml'
                }
            }
        }


        // =========================================================
        // 3. FASTAPI SONARQUBE
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
                                -Dsonar.exclusions="venv/**,tests/**,__pycache__/**" \
                                -Dsonar.python.coverage.reportPaths=coverage.xml
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 4. FASTAPI QUALITY GATE
        // =========================================================

        stage('FastAPI - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 5. SPRING BOOT TEST
        // =========================================================

        stage('Backend - Test') {
            steps {

                dir('stroke_backend') {

                    sh '''
                        echo "======================================"
                        echo "Spring Boot Tests"
                        echo "======================================"

                        chmod +x mvnw

                        ./mvnw clean verify
                    '''
                }
            }

            post {
                always {

                    junit allowEmptyResults: true,
                          testResults: 'stroke_backend/target/surefire-reports/*.xml'
                }
            }
        }


        // =========================================================
        // 6. SPRING BOOT SONARQUBE
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
                                -Dsonar.projectName="Stroke Backend"
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 7. BACKEND QUALITY GATE
        // =========================================================

        stage('Backend - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 8. ANGULAR TEST
        // =========================================================

        stage('Frontend - Test') {
            steps {

                dir('stroke_frontend') {

                    sh '''
                        echo "======================================"
                        echo "Angular Tests"
                        echo "======================================"

                        node --version
                        npm --version

                        npm ci

                        npm test -- \
                            --watch=false \
                            --browsers=ChromeHeadless \
                            --code-coverage
                    '''
                }
            }
        }


        // =========================================================
        // 9. ANGULAR SONARQUBE
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
                                -Dsonar.exclusions="node_modules/**,dist/**" \
                                -Dsonar.javascript.lcov.reportPaths=coverage/**/lcov.info
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 10. FRONTEND QUALITY GATE
        // =========================================================

        stage('Frontend - Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 11. DOCKER BUILD - FASTAPI
        // =========================================================

        stage('Docker - FastAPI') {
            steps {

                dir('fastapi-service') {

                    sh '''
                        echo "Building FastAPI Docker image..."

                        docker build \
                            -t ${FASTAPI_IMAGE} .
                    '''
                }
            }
        }


        // =========================================================
        // 12. DOCKER BUILD - BACKEND
        // =========================================================

        stage('Docker - Backend') {
            steps {

                dir('stroke_backend') {

                    sh '''
                        echo "Building Spring Boot Docker image..."

                        docker build \
                            -t ${BACKEND_IMAGE} .
                    '''
                }
            }
        }


        // =========================================================
        // 13. DOCKER BUILD - FRONTEND
        // =========================================================

        stage('Docker - Frontend') {
            steps {

                dir('stroke_frontend') {

                    sh '''
                        echo "Building Angular Docker image..."

                        docker build \
                            -t ${FRONTEND_IMAGE} .
                    '''
                }
            }
        }


        // =========================================================
        // 14. DOCKER PUSH
        // =========================================================

        /*
        stage('Docker - Push') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        docker push ${FASTAPI_IMAGE}
                        docker push ${BACKEND_IMAGE}
                        docker push ${FRONTEND_IMAGE}

                        docker logout
                    '''
                }
            }
        }
        */

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
            Tests:       PASSED
            SonarQube:   PASSED
            QualityGate: PASSED
            Docker:      BUILT
            ==========================================
            '''
        }

        failure {
            echo '''
            ==========================================
            PIPELINE FAILED
            ==========================================
            Check the failed stage above.
            ==========================================
            '''
        }

        always {
            echo 'CI/CD pipeline finished.'
        }
    }
}