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
    image: lachlanevenson/k8s-helm:v3.0.0-beta.3
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
                stage('checkout') {
                    checkout scm

                    stage('application project') {

                        container('docker') {
                            env.DOCKER_TAG = "${BRANCH_NAME}_02_${BUILD_NUMBER}"
                            stage('build application') {
                                sh 'docker build -f install/Dockerfile -t localhost:32000/scraper-backend:${DOCKER_TAG} .'
                            }
                            stage('publish application') {
                                sh 'docker push localhost:32000/scraper-backend:${DOCKER_TAG}'
                            }
                            stage('rm application') {}
                        }
                        container('k8s-helm') {
                            stage('SET ENV') {
                                if (env.BRANCH_NAME == 'dev') {
                                    env.VALUES_FILE = 'values.yaml'
                                }
                                if (env.BRANCH_NAME == 'test') {
                                    env.VALUES_FILE = 'values-test.yaml'
                                }
                                if (env.BRANCH_NAME == 'master') {
                                    env.VALUES_FILE = 'values-prod.yaml'
                                }
                            }
                            stage('helm upgrade backend') {
                                def now = new Date()
                                def dateFormatted = now.format("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                sh "helm upgrade --install backend \
                                    -f install/helm/backend/values.yaml \
                                    -f install/helm/backend/${VALUES_FILE} \
                                    --version 2.0.${BUILD_NUMBER}\
                                    --namespace scraper \
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