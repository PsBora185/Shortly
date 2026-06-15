pipeline {
    agent { label 'project' }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        K8S_NAMESPACE = 'shortly'
        K8S_DIR       = 'k8s'
        KUBECONFIG    = '/home/ubuntu/.kube/config'
        TAG           = "v${BUILD_NUMBER}"
        APP_NAME      = 'shortly'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Cleanup Old Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        # Remove old app images (keep none - fresh build every time)
                        docker images --format '{{.Repository}}:{{.Tag}}' | \\
                            grep -E '${DOCKER_USER}/${APP_NAME}-(backend|frontend)' | \\
                            xargs -r docker rmi -f || true

                        # Remove dangling/unused images
                        docker image prune -f || true
                    """
                }
            }
        }

        stage('Build Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'docker build -t ${DOCKER_USER}/${APP_NAME}-backend:${TAG} ./backend'
                    sh 'docker build -t ${DOCKER_USER}/${APP_NAME}-frontend:${TAG} ./frontend'
                }
            }
        }

        stage('Push Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push ${DOCKER_USER}/${APP_NAME}-backend:${TAG}'
                    sh 'docker push ${DOCKER_USER}/${APP_NAME}-frontend:${TAG}'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS'),
                    usernamePassword(credentialsId: 'gmail', usernameVariable: 'GMAIL_USER', passwordVariable: 'GMAIL_PASS'),
                    string(credentialsId: 'jwt', variable: 'JWT_SECRET')
                ]) {
                    sh """
                        # Create Namespace and Secrets
                        kubectl apply -f ${K8S_DIR}/namespace.yaml
                        
                        kubectl create secret generic ${APP_NAME}-secrets \\
                            --from-literal=SPRING_MAIL_USERNAME="\$GMAIL_USER" \\
                            --from-literal=SPRING_MAIL_PASSWORD="\$GMAIL_PASS" \\
                            --from-literal=JWT_SECRET="\$JWT_SECRET" \\
                            -n ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                        # Apply Base Configurations
                        kubectl apply -f ${K8S_DIR}/postgres.yaml
                        kubectl apply -f ${K8S_DIR}/backend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/backend-service.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-service.yaml

                        # Update Pods with New Version
                        kubectl set image deployment/shortly-backend shortly-backend=\${DOCKER_USER}/shortly-backend:\${TAG} -n ${K8S_NAMESPACE}
                        kubectl set image deployment/shortly-frontend shortly-frontend=\${DOCKER_USER}/shortly-frontend:\${TAG} -n ${K8S_NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                # Remove newly built images after push to free disk space
                docker images --format '{{.Repository}}:{{.Tag}}' | \\
                    grep -E '${APP_NAME}-(backend|frontend)' | \\
                    xargs -r docker rmi -f || true
                docker image prune -f || true
            """
        }
        success {
            script {
                // Get the public IP of the EC2 instance
                def publicIp = sh(script: 'curl -s ifconfig.me', returnStdout: true).trim()
                
                echo "Deployed successfully live on EC2!"
                emailext(
                    subject: "SUCCESS: Shortly Pipeline Build #${BUILD_NUMBER}",
                    body: "The deployment was successful!\n\nShortly is live on: http://${publicIp}:30080\n\nCheck Jenkins for details: ${BUILD_URL}",
                    to: "pranavsinghbora@gmail.com"
                )
            }
        }
        failure {
            emailext(
                subject: "FAILED: Shortly Pipeline Build #${BUILD_NUMBER}",
                body: "The pipeline failed to deploy.\n\nPlease check the Jenkins logs to fix the issue: ${BUILD_URL}",
                to: "pranavsinghbora@gmail.com"
            )
        }
    }
}
