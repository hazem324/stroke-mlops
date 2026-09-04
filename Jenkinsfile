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
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        dir('stroke_backend') {
                            timeout(time: 20, unit: 'MINUTES') {
                                sh '''
                                    set -e
                                    chmod +x mvnw
                                    ./mvnw test -q
                                '''
                            }
                        }
                    }
                }

                stage('Frontend Tests') {
                    steps {
                        dir('stroke_frontend') {
                            timeout(time: 20, unit: 'MINUTES') {
                                sh '''
                                    set -e
                                    test -f package.json
                                    if ! command -v chromium >/dev/null 2>&1; then
                                        apt-get update
                                        DEBIAN_FRONTEND=noninteractive apt-get install -y chromium chromium-driver
                                    fi
                                    export CHROME_BIN="$(command -v chromium || command -v google-chrome || command -v chromium-browser)"
                                    npm ci --no-audit --progress=false
                                    npm test -- --watch=false --browsers=ChromeHeadlessNoSandbox --code-coverage --progress=false --source-map=false
                                '''
                            }
                        }
                    }
                }

                stage('FastAPI Tests') {
                    steps {
                        dir('fastapi-service') {
                            timeout(time: 20, unit: 'MINUTES') {
                                sh '''
                                    set -e
                                    if ! command -v python3 >/dev/null 2>&1; then
                                        exit 1
                                    fi
                                    if [ ! -d .venv ]; then
                                        python3 -m ensurepip --upgrade || true
                                        python3 -m venv .venv || python3 -m pip install --user virtualenv
                                        [ -d .venv ] || python3 -m virtualenv .venv
                                    fi
                                    . .venv/bin/activate
                                    python -m pip install --disable-pip-version-check -r requirements.txt pytest pytest-cov httpx
                                    python -m pytest tests -q --cov=app --cov-report=term-missing --cov-report=xml:coverage.xml
                                '''
                            }
                        }
                    }
                }
            }
        }

        stage('Backend - SonarQube') {
            steps {
                dir('stroke_backend') {
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh '''
                            chmod +x mvnw
                            ./mvnw sonar:sonar \
                                -Dsonar.projectKey=stroke-backend \
                                -Dsonar.projectName="Stroke Backend" \
                                -Dsonar.sources=src/main/java \
                                -Dsonar.tests=src/test/java \
                                -Dsonar.java.binaries=target/classes,target/test-classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.exclusions="**/generated/**,**/target/**"
                        '''
                    }
                }
            }
        }

        stage('Frontend - SonarQube') {
            steps {
                dir('stroke_frontend') {
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh '''
                            sonar-scanner \
                                -Dsonar.projectKey=stroke-frontend \
                                -Dsonar.projectName="Stroke Frontend" \
                                -Dsonar.sources=src/app \
                                -Dsonar.tests=src/app \
                                -Dsonar.test.inclusions="**/*.spec.ts" \
                                -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info \
                                -Dsonar.exclusions="**/*.spec.ts,**/node_modules/**,**/dist/**,**/coverage/**"
                        '''
                    }
                }
            }
        }

        stage('FastAPI - SonarQube') {
            steps {
                dir('fastapi-service') {
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh '''
                            sonar-scanner \
                                -Dsonar.projectKey=stroke-fastapi \
                                -Dsonar.projectName="Stroke FastAPI" \
                                -Dsonar.sources=app \
                                -Dsonar.tests=tests \
                                -Dsonar.python.coverage.reportPaths=coverage.xml \
                                -Dsonar.exclusions="**/__pycache__/**,**/.venv/**,**/outputs/**,**/models/**,**/*.pth,**/*.nii*,**/*.png,**/*.jpg"
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build and Push') {
            steps {
                sh '''
                    echo "Docker build and push not configured for this workspace."
                '''
            }
        }
    }

    post {
        success {
            echo 'CI pipeline completed successfully.'
        }
        failure {
            echo 'CI pipeline failed. Review the stage logs and SonarQube quality gate.'
        }
    }
}
