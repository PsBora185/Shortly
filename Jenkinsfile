pipeline {
    agent { label 'project' }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        NAMESPACE     = 'shortly'
        K8S_DIR       = 'k8s'
        KUBECONFIG    = '/home/ubuntu/.kube/config'
        TAG           = "v${BUILD_NUMBER}"
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

        stage('Build Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'docker build -t ${DOCKER_USER}/shortly-backend:${TAG} ./backend'
                    sh 'docker build -t ${DOCKER_USER}/shortly-frontend:${TAG} ./frontend'
                }
            }
        }

        stage('Push Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push ${DOCKER_USER}/shortly-backend:${TAG}'
                    sh 'docker push ${DOCKER_USER}/shortly-frontend:${TAG}'
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
                        
                        kubectl create secret generic app-secrets \\
                            --from-literal=SPRING_MAIL_USERNAME="\$GMAIL_USER" \\
                            --from-literal=SPRING_MAIL_PASSWORD="\$GMAIL_PASS" \\
                            --from-literal=JWT_SECRET="\$JWT_SECRET" \\
                            -n ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                        # Apply Base Configurations
                        kubectl apply -f ${K8S_DIR}/postgres.yaml
                        kubectl apply -f ${K8S_DIR}/backend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/backend-service.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-deployment.yaml
                        kubectl apply -f ${K8S_DIR}/frontend-service.yaml

                        # Update Pods with New Version
                        kubectl set image deployment/shortly-backend shortly-backend=\${DOCKER_USER}/shortly-backend:\${TAG} -n ${NAMESPACE}
                        kubectl set image deployment/shortly-frontend shortly-frontend=\${DOCKER_USER}/shortly-frontend:\${TAG} -n ${NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                docker image prune -f || true
            """
        }
    }
}
