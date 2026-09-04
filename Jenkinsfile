pipeline {

    agent any

    environment {
        SONARQUBE = 'SonarQube'

        // Docker images utilisées uniquement pour les tests
        FASTAPI_TEST_IMAGE = 'python:3.12-slim'
        FRONTEND_TEST_IMAGE = 'trion/ng-cli-karma:latest'

        // MySQL utilisé pendant les tests Spring Boot
        MYSQL_TEST_CONTAINER = 'stroke-mysql-test'
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

                // =================================================
                // FASTAPI TESTS
                // =================================================

                stage('FastAPI Tests') {
    steps {
        dir('fastapi-service') {
            timeout(time: 20, unit: 'MINUTES') {
                sh '''
                    set -e

                    echo "======================================"
                    echo "FastAPI Tests"
                    echo "======================================"

                    echo "Current directory:"
                    pwd

                    echo "FastAPI project files:"
                    ls -la

                    echo "Checking required files..."

                    test -f requirements.txt
                    test -f run.py
                    test -d tests
                    test -f tests/test_health.py

                    echo "Running FastAPI tests inside Docker..."

                    docker run --rm \
                        -v "$(pwd):/app" \
                        -w /app \
                        python:3.12-slim \
                        sh -c '
                            set -e

                            echo "======================================"
                            echo "Inside Python Docker container"
                            echo "======================================"

                            echo "Python version:"
                            python --version

                            echo "Installing dependencies..."
                            pip install --no-cache-dir -r requirements.txt

                            echo "Installing pytest..."
                            pip install --no-cache-dir pytest

                            echo "Running tests..."
                            python -m pytest tests/ --verbose
                        '

                    echo "======================================"
                    echo "FastAPI tests completed successfully."
                    echo "======================================"
                '''
            }
        }
    }
}


                // =================================================
                // BACKEND TESTS
                // =================================================

                stage('Backend Tests') {

    steps {

        dir('stroke_backend') {

            sh '''
                set -e

                echo "======================================"
                echo "Spring Boot Tests"
                echo "======================================"

                chmod +x mvnw

                ./mvnw clean test \
                    -Dspring.profiles.active=test

                echo "Backend tests completed successfully."
            '''
        }
    }
}


                // =================================================
                // FRONTEND TESTS
                // =================================================

             stage('Frontend Tests') {

    steps {

        dir('stroke_frontend') {

            timeout(time: 20, unit: 'MINUTES') {

                sh '''
                    set -e

                    echo "======================================"
                    echo "Angular Tests"
                    echo "======================================"

                    echo "Current directory:"
                    pwd

                    echo "======================================"
                    echo "Checking frontend files"
                    echo "======================================"

                    ls -la

                    test -f package.json
                    test -f package-lock.json

                    echo "package.json found:"
                    ls -lh package.json

                    echo "package-lock.json found:"
                    ls -lh package-lock.json

                    echo "======================================"
                    echo "Node.js version"
                    echo "======================================"

                    node --version

                    echo "npm version:"
                    npm --version

                    echo "======================================"
                    echo "Installing dependencies"
                    echo "======================================"

                    npm ci \
                        --prefer-offline \
                        --no-audit \
                        --progress=false

                    echo "======================================"
                    echo "Running Angular tests"
                    echo "======================================"

                    npm run test -- \
                        --watch=false \
                        --code-coverage \
                        --browsers=ChromeHeadlessNoSandbox \
                        --source-map=false \
                        --progress=false

                    echo "======================================"
                    echo "Angular tests completed successfully."
                    echo "======================================"
                '''
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
    // POST ACTIONS
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
              - FastAPI tests failed
              - Backend tests failed
              - Frontend tests failed
              - SonarQube analysis failed
              - SonarQube Quality Gate failed

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