pipeline {
    agent { label 'project' }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        K8S_DIR       = 'k8s'
        KUBECONFIG    = '/home/ubuntu/.kube/config'
        TAG           = "v${BUILD_NUMBER}"
        APP_NAME      = 'shortly'
    }

    stages {

        stage('public ip'){
            steps{
                script {
            env.IP = sh(
                script: 'curl -s ifconfig.me',
                returnStdout: true
            ).trim()
        }
            }
        }

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
                
                    sh """
                        docker images --format '{{.Repository}}:{{.Tag}}' | \\
                            grep -E '${APP_NAME}-(backend|frontend)' | \\
                            xargs -r docker rmi -f || true

                        docker image prune -f || true
                    """
                
            }
        }

        stage('Build Images') {
            steps {
                    sh 'docker build -t psbora185/${APP_NAME}-backend:${TAG} ./backend'
                    sh 'docker build -t psbora185/${APP_NAME}-frontend:${TAG} ./frontend'
                }
            }

        stage('Push Images') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub', 
                        usernameVariable: 'docker_user', 
                        passwordVariable: 'docker_pass')
                        ]) {
                    sh"""
                        echo "$docker_pass" | docker login -u "$docker_user" --password-stdin
                        """
                    sh "docker push ${docker_user}/${APP_NAME}-backend:${TAG}"
                    sh "docker push ${docker_user}/${APP_NAME}-frontend:${TAG}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'gmail', usernameVariable: 'gmail_user', passwordVariable: 'gmail_pass'),
                    string(credentialsId: 'jwt', variable: 'jwt_sec')
                ]) {
                    sh """
                        kubectl apply -f ${K8S_DIR}/namespace.yaml
                        
                        kubectl create secret generic ${APP_NAME}-secrets \\
                            --from-literal=SPRING_MAIL_USERNAME="\$gmail_user" \\
                            --from-literal=SPRING_MAIL_PASSWORD="\$gmail_pass" \\
                            --from-literal=JWT_SECRET="\$jwt_sec" \\
                            --from-literal=BACKEND_URL="http://${IP}:30081" \\
                            --from-literal=FRONTEND_URL="http://${IP}:30080" \\
                            -n shortly --dry-run=client -o yaml | kubectl apply -f -

                        kubectl apply -f ${K8S_DIR}/

                        kubectl set image deployment/shortly-backend shortly-backend=psbora185/shortly-backend:\${TAG} -n shortly
                        kubectl set image deployment/shortly-frontend shortly-frontend=psbora185/shortly-frontend:\${TAG} -n shortly
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                docker images --format '{{.Repository}}:{{.Tag}}' | \\
                    grep -E '${APP_NAME}-(backend|frontend)' | \\
                    xargs -r docker rmi -f || true
                docker image prune -f || true
            """
        }
        success {
            script {
                
                echo "Deployed successfully live on EC2!"
                emailext(
                    subject: "SUCCESS: Shortly Pipeline Build #${BUILD_NUMBER}",
                    body: "The deployment was successful!\n\nShortly is live on: http://${IP}:30080\n\nCheck Jenkins for details: ${BUILD_URL}",
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
