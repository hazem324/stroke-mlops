pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
        DOCKER_NAMESPACE = "hazemhadda231"
        IMAGE_VERSION = "1.${BUILD_NUMBER}"
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
                            timeout(time: 600, unit: 'MINUTES') {
                                sh '''
                                    set -e

                                    chmod +x mvnw

                                    ./mvnw clean verify -q
                                '''
                            }
                        }
                    }
                }

                stage('Frontend Tests') {
                    steps {
                        dir('stroke_frontend') {
                            timeout(time: 600, unit: 'MINUTES') {
                                sh '''
                                    set -e

                                    test -f package.json

                                    command -v chromium
                                    chromium --version

                                    export CHROME_BIN="$(command -v chromium)"

                                    npm ci --no-audit --progress=false

                                    npm test -- \
                                        --watch=false \
                                        --browsers=ChromeHeadlessNoSandbox \
                                        --code-coverage \
                                        --progress=false \
                                        --source-map=false

                                    echo "======================================"
                                    echo "Coverage files generated:"
                                    echo "======================================"

                                    find coverage -type f -print

                                    echo "======================================"
                                '''
                            }
                        }
                    }
                }

                stage('FastAPI Tests') {
                    steps {
                        dir('fastapi-service') {
                            timeout(time: 600, unit: 'MINUTES') {
                                sh '''
                                    set -e

                                    if ! command -v python3 >/dev/null 2>&1; then
                                        echo "Python 3 is required for FastAPI tests but is not installed on this Jenkins agent."
                                        exit 1
                                    fi

                                    install_python_venv() {
                                        if command -v sudo >/dev/null 2>&1; then
                                            sudo apt-get update
                                            sudo DEBIAN_FRONTEND=noninteractive apt-get install -y python3-venv python3-pip python3-dev
                                            return 0
                                        fi

                                        if [ "$(id -u)" = "0" ]; then
                                            apt-get update
                                            DEBIAN_FRONTEND=noninteractive apt-get install -y python3-venv python3-pip python3-dev
                                            return 0
                                        fi

                                        return 1
                                    }

                                    rm -rf .venv

                                    if python3 -m venv .venv >/dev/null 2>&1; then
                                        :
                                    elif python3 -m ensurepip --upgrade >/dev/null 2>&1; then
                                        python3 -m venv .venv
                                    elif python3 -m pip --version >/dev/null 2>&1; then
                                        python3 -m pip install --user --upgrade pip virtualenv
                                        python3 -m virtualenv .venv
                                    elif install_python_venv; then
                                        python3 -m venv .venv
                                    else
                                        echo "Python venv support is required for FastAPI tests and this Jenkins agent cannot install system packages."
                                        echo "Please use an agent with Python 3 + venv support, or pre-create a working virtual environment on the node."
                                        exit 1
                                    fi

                                    . .venv/bin/activate

                                    python -m pip install --disable-pip-version-check --upgrade pip

                                    python -m pip install \
                                        --disable-pip-version-check \
                                        -r requirements.txt \
                                        -r requirements-test.txt \
                                        httpx

                                    python -m pytest tests -q \
                                        --cov=app \
                                        --cov-report=term-missing \
                                        --cov-report=xml:coverage.xml
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
                            set -e

                            echo "======================================"
                            echo "Backend - SonarQube"
                            echo "======================================"

                            chmod +x mvnw

                            ./mvnw sonar:sonar \
                                -Dsonar.projectKey=stroke-backend \
                                -Dsonar.projectName="Stroke Backend" \
                                -Dsonar.sources=src/main/java \
                                -Dsonar.tests=src/test/java \
                                -Dsonar.java.binaries=target/classes,target/test-classes \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.exclusions=**/generated/**,**/target/**

                            echo "Backend SonarQube analysis completed."
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
                            set -e

                            echo "======================================"
                            echo "Frontend - SonarQube"
                            echo "======================================"

                            echo "Checking Node/npm..."
                            node --version
                            npm --version

                            echo "Checking sonar-scanner..."
                            npx sonar-scanner --version

                            echo "Searching for LCOV coverage..."
                            find coverage -type f -name "lcov.info" -print

                            LCOV_FILE=$(find coverage -type f -name "lcov.info" | head -n 1)

                            if [ -z "$LCOV_FILE" ]; then
                                echo "ERROR: lcov.info not found!"
                                echo "Contents of coverage directory:"
                                find coverage -maxdepth 3 -type f -print || true
                                exit 1
                            fi

                            echo "Found LCOV file: $LCOV_FILE"

                            echo "Running SonarQube analysis..."

                            npx sonar-scanner \
                                -Dsonar.projectKey=Stroke-Frontend \
                                -Dsonar.projectName="Stroke Frontend" \
                                -Dsonar.sources=src/app \
                                -Dsonar.tests=src/app \
                                -Dsonar.test.inclusions="**/*.spec.ts" \
                                -Dsonar.exclusions="**/*.spec.ts,**/node_modules/**,**/dist/**,**/coverage/**" \
                                -Dsonar.javascript.lcov.reportPaths="$LCOV_FILE" \
                                -Dsonar.sourceEncoding=UTF-8

                            echo "Frontend SonarQube analysis completed."
                        '''
                    }
                }
            }
        }


        stage('FastAPI - SonarQube') {
            steps {
                dir('fastapi-service') {

                    withSonarQubeEnv("${SONARQUBE}") {

                        withEnv([
                            "PATH+SONAR=${tool 'sonar-scanner'}/bin"
                        ]) {

                            sh '''
                                set -e

                                echo "======================================"
                                echo "FastAPI - SonarQube"
                                echo "======================================"

                                echo "Checking sonar-scanner..."
                                which sonar-scanner
                                sonar-scanner --version

                                echo "Checking coverage..."
                                test -f coverage.xml
                                echo "coverage.xml found"

                                echo "Running SonarQube analysis..."

                                sonar-scanner \
                                    -Dsonar.projectKey=stroke-fastapi \
                                    -Dsonar.projectName="Stroke FastAPI" \
                                    -Dsonar.sources=app \
                                    -Dsonar.tests=tests \
                                    -Dsonar.python.coverage.reportPaths=coverage.xml \
                                    -Dsonar.exclusions="**/__pycache__/**,**/.venv/**,**/outputs/**,**/models/**,**/*.pth,**/*.nii*,**/*.png,**/*.jpg"

                                echo "FastAPI SonarQube analysis completed."
                            '''
                        }
                    }
                }
            }
        }


        stage('Quality Gate') {
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "Docker build images"
                    echo "Building frontend Docker images version ${IMAGE_VERSION}"
                    docker build -t  ${DOCKER_NAMESPACE}/stroke-frontend:${IMAGE_VERSION} ./stroke_frontend
                    echo "======================================"
                    echo "Building backend Docker images version ${IMAGE_VERSION}"
                    docker build -t  ${DOCKER_NAMESPACE}/stroke-backend:${IMAGE_VERSION} ./stroke_backend
                    echo "======================================"
                    echo "Building fastapi Docker images version ${IMAGE_VERSION}"
                     echo "FastAPI Docker image build skipped temporarily"
                   
                '''
                /* docker build -t  ${DOCKER_NAMESPACE}/stroke-fastapi:${IMAGE_VERSION} ./fastapi-service*/
            }
        }

        stage('Docker push to registry') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ])

                sh '''
                        set -e

                        echo "$DOCKER_PASS" | docker login  -u "$DOCKER_USER" --password-stdin

                        echo "Pushing Backend Image..."
                        docker push ${DOCKER_NAMESPACE}/stroke-backend:${IMAGE_VERSION}
                        echo "======================================"

                        echo "Pushing Frontend Image..."
                        docker push ${DOCKER_NAMESPACE}/stroke-frontend:${IMAGE_VERSION}
                        echo "======================================"

                        echo "Pushing fastapi service Image..."
                         echo "FastAPI image push skipped temporarily"

                        docker logout

                        echo "All Docker Images Pushed Successfully"
                    '''
                    /* docker push ${DOCKER_NAMESPACE}/stroke-fastapi:${IMAGE_VERSION} */ 
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