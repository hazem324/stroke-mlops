pipeline {

    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
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
        // 2. TESTS
        // =========================================================

        stage('Tests') {

            parallel {

                // -------------------------------------------------
                // FASTAPI TESTS
                // -------------------------------------------------

                stage('FastAPI Tests') {

                    steps {

                        dir('fastapi-service') {

                            sh '''
                                echo "======================================"
                                echo "FastAPI Tests"
                                echo "======================================"

                                python3 -m pytest tests/ \
                                    --verbose
                            '''
                        }
                    }
                }


                // -------------------------------------------------
                // BACKEND TESTS
                // -------------------------------------------------

                stage('Backend Tests') {

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
                }


                // -------------------------------------------------
                // FRONTEND TESTS
                // -------------------------------------------------

                stage('Frontend Tests') {

                    steps {

                        dir('stroke_frontend') {

                            timeout(time: 20, unit: 'MINUTES') {

                                withEnv(['PUPPETEER_SKIP_DOWNLOAD=false']) {

                                    sh '''
                                        echo "======================================"
                                        echo "Angular Tests"
                                        echo "======================================"

                                        npm ci --prefer-offline --no-audit --progress=false

                                        npm run test -- \
                                            --watch=false \
                                            --code-coverage \
                                            --browsers=ChromeHeadlessNoSandbox \
                                            --source-map=false \
                                            --progress=false
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }


        // =========================================================
        // 3. FASTAPI - SONARQUBE
        // =========================================================

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
                                    -Dsonar.exclusions="venv/**,tests/**,__pycache__/**,.pytest_cache/**,outputs/**,models/**,coverage/**,**/*.png,**/*.jpg,**/*.jpeg,**/*.nii,**/*.nii.gz,**/*.pth,**/*.pt,**/*.h5,**/*.pkl,**/*.coverage"
                            """
                        }
                    }
                }
            }
        }


        // =========================================================
        // 4. FASTAPI - QUALITY GATE
        // =========================================================

        stage('FastAPI - Quality Gate') {

            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 5. BACKEND - SONARQUBE
        // =========================================================

        stage('Backend - SonarQube') {

            steps {

                dir('stroke_backend') {

                    withSonarQubeEnv("${SONARQUBE}") {

                        sh '''
                            echo "======================================"
                            echo "Spring Boot SonarQube Analysis"
                            echo "======================================"

                            chmod +x mvnw

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
        // 6. BACKEND - QUALITY GATE
        // =========================================================

        stage('Backend - Quality Gate') {

            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 7. FRONTEND - SONARQUBE
        // =========================================================

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


        // =========================================================
        // 8. FRONTEND - QUALITY GATE
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

            Tests:
              FastAPI   : PASSED
              Backend   : PASSED
              Frontend  : PASSED

            SonarQube:
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

            Check the failed stage.

            Possible causes:
              - Tests failed
              - SonarQube analysis failed
              - Quality Gate failed

            ==========================================
            '''
        }


        aborted {

            echo '''
            ==========================================
                    PIPELINE ABORTED
            ==========================================
            '''
        }


        always {

            echo 'CI/CD pipeline finished.'
        }
    }
}