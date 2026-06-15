pipeline {
    agent { label 'project' }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        NAMESPACE     = 'shortly'
        K8S_DIR       = 'k8s'
        DOCKER_CREDS  = 'dockerhub'
    }

    stages {

        // ─────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ─────────────────────────────────────────────
        stage('Build & Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Prepare Image Tags') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDS}",
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    script {
                        def sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        env.TAG           = "${env.BUILD_NUMBER}-${sha}"
                        env.BACKEND_IMG   = "${DOCKER_USER}/shortly-backend"
                        env.FRONTEND_IMG  = "${DOCKER_USER}/shortly-frontend"
                    }
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Docker Build') {
            parallel {
                stage('Backend Image') {
                    steps {
                        dir('backend') {
                            sh """
                                docker build \\
                                  -t ${BACKEND_IMG}:${TAG} \\
                                  -t ${BACKEND_IMG}:latest \\
                                  .
                            """
                        }
                    }
                }
                stage('Frontend Image') {
                    steps {
                        dir('frontend') {
                            sh """
                                docker build \\
                                  -t ${FRONTEND_IMG}:${TAG} \\
                                  -t ${FRONTEND_IMG}:latest \\
                                  .
                            """
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDS}",
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin

                        docker push ${BACKEND_IMG}:${TAG}
                        docker push ${BACKEND_IMG}:latest

                        docker push ${FRONTEND_IMG}:${TAG}
                        docker push ${FRONTEND_IMG}:latest
                    """
                }
            }
        }

        // ─────────────────────────────────────────────
        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'gmail', usernameVariable: 'GMAIL_USER', passwordVariable: 'GMAIL_PASS'),
                    string(credentialsId: 'jwt',             variable: 'JWT_SECRET')
                ]) {
                    sh """
                        # Namespace
                        kubectl apply -f ${K8S_DIR}/namespace.yaml

                        # Secrets (idempotent)
                        kubectl create secret generic app-secrets \\
                            --from-literal=SPRING_MAIL_USERNAME="\$GMAIL_USER" \\
                            --from-literal=SPRING_MAIL_PASSWORD="\$GMAIL_PASS" \\
                            --from-literal=JWT_SECRET="\$JWT_SECRET" \\
                            -n ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                        # Apply manifests
                        kubectl apply -f ${K8S_DIR}/backend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/backend-service.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-service.yaml

                        # Roll out new images
                        kubectl set image deployment/shortly-backend  shortly-backend=${BACKEND_IMG}:${TAG}  -n ${NAMESPACE}
                        kubectl set image deployment/shortly-frontend shortly-frontend=${FRONTEND_IMG}:${TAG} -n ${NAMESPACE}

                        # Wait for rollouts
                        kubectl rollout status deployment/shortly-backend  -n ${NAMESPACE}
                        kubectl rollout status deployment/shortly-frontend -n ${NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
