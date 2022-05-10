#!groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
def label = "frontend-${UUID.randomUUID().toString()}"
podTemplate(label: label, yaml: """
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
"""
)
        {
            node(label) {
                properties([disableConcurrentBuilds()])

                stage('checkout') {
                    checkout scm

                    stage('application project') {

                        container('docker') {
                            env.DOCKER_TAG = "${BRANCH_NAME}_02_${BUILD_NUMBER}"
                            stage('build application') {
                                sh 'docker login  \
                                    -u ${DOCKER_CONTAINER_LOGIN}  \
                                    -p ${DOCKER_CONTAINER_PASSWORD}'
                                sh 'docker build -f install/Dockerfile -t ${DOCKER_CONTAINER_PREFIX}/scraper-backend:${DOCKER_TAG} .'
                            }
                            stage('publish application') {
                                sh 'docker push ${DOCKER_CONTAINER_PREFIX}/scraper-backend:${DOCKER_TAG}'
                            }
                            stage('rm application') {}
                        }
                        container('k8s-helm') {

                            stage('helm upgrade backend') {
                                def now = new Date()
                                def dateFormatted = now.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                sh "helm template --dry-run --debug backend \
                                    -f install/helm/backend/values.yaml \
                                    -f install/helm/backend/${VALUES_FILE} \
                                    --version 2.0.${BUILD_NUMBER}\
                                    --namespace app \
                                    --set image.repository=${DOCKER_CONTAINER_PREFIX}/scraper-backend \
                                    --set image.tag=${DOCKER_TAG} \
                                    --set APP_VERSION=2.0.${BUILD_NUMBER} \
                                    --set APP_LAST_UPDATED=${dateFormatted} \
                                    install/helm/backend"
                                sh "helm upgrade --install backend \
                                    -f install/helm/backend/values.yaml \
                                    -f install/helm/backend/${VALUES_FILE} \
                                    --version 2.0.${BUILD_NUMBER}\
                                    --namespace app \
                                    --set image.repository=${DOCKER_CONTAINER_PREFIX}/scraper-backend \
                                    --set image.tag=${DOCKER_TAG} \
                                    --set APP_VERSION=2.0.${BUILD_NUMBER} \
                                    --set APP_LAST_UPDATED=${dateFormatted} \
                                    install/helm/backend"
                            }
                        }
                    }
                }
            }
        }