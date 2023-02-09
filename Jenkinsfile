pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
  - name: kubeconfig
    hostPath:
      path: /home/s/.kube
  containers:
  - name: k8s-helm
    image: lachlanevenson/k8s-helm:v3.6.0
    command:
    - cat
    tty: true
    volumeMounts:
      - name: kubeconfig
        mountPath: "/home/s/.kube"
  - name: docker
    image: docker
    volumeMounts:
      - name: dockersock
        mountPath: "/var/run/docker.sock"
    command:
    - cat
    tty: true
'''
        }
    }
    environment {
        DISABLE_AUTH = 'true'
        GIT_COMMIT_HASH = sh "(git log -n 1 --pretty=format:'%H')"
    }
    stages {
        stage('docker build & push') {
            steps {
                checkout scm
                container('docker') {
                    sh 'docker login -u bombascter -p "!Prisoner31!"'
                    sh 'docker build -f install/Dockerfile -t bombascter/scraper-backend:${GIT_COMMIT_HASH} .'
                    sh 'docker push bombascter/scraper-backend:${GIT_COMMIT_HASH}'
                }
            }
        }
        stage('helm') {
            steps {
                container('k8s-helm') {
//                     def now = new Date()
//                     def dateFormatted = now.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    sh "helm template --dry-run --debug backend \
                        -f install/helm/backend/values-production.yaml \
                        --version 2.0.${currentBuild.number}\
                        --namespace app \
                        --set image.repository=bombascter/scraper-backend \
                        --set image.tag=${DOCKER_TAG} \
                        --set APP_VERSION=2.0.${currentBuild.number} \
                        --set APP_LAST_UPDATED=${dateFormatted} \
                        install/helm/backend"
                    sh "helm upgrade --install backend \
                        -f install/helm/backend/values-production.yaml \
                        --version 2.0.${currentBuild.number}\
                        --namespace app \
                        --set image.repository=bombascter/scraper-backend \
                        --set image.tag=${DOCKER_TAG} \
                        --set APP_VERSION=2.0.${currentBuild.number} \
                        --set APP_LAST_UPDATED=${dateFormatted} \
                        install/helm/backend"
                }
            }
        }
    }
}
