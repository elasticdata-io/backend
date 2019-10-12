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
      path: /opt/kubernetes/storage/.kube
  containers:
  - name: k8s-helm
    image: lachlanevenson/k8s-helm:v3.0.0-beta.3
    command:
    - cat
    tty: true
    volumeMounts:
      - name: kubeconfig
        mountPath: "/opt/.kube"
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

                    dir('scraper-core') {
                        checkout([$class: 'GitSCM',
                            userRemoteConfigs: [[credentialsId: 'github-user-password', url: 'https://github.com/sergeytkachenko/scraper-core.git']]
                        ])
                    }

                    stage('application project') {

                        container('docker') {
                            env.DOCKER_TAG = "${BRANCH_NAME}_${BUILD_NUMBER}"
                            stage('build application') {
                                sh 'docker build -f install/Dockerfile -t localhost:32000/scraper-backend:${DOCKER_TAG} .'
                            }
                            stage('publish application') {
                                sh 'docker push localhost:32000/scraper-backend:${DOCKER_TAG}'
                            }
                            stage('rm application') {

                            }

                            stage('build docs') {
                                sh 'docker build -f install/Docs.Dockerfile -t localhost:32000/docs:${DOCKER_TAG} .'
                            }
                            stage('publish application') {
                                sh 'docker push localhost:32000/docs:${DOCKER_TAG}'
                            }
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
                                sh "helm upgrade --install backend \
                                    -f install/helm/backend/values.yaml \
                                    -f install/helm/backend/${VALUES_FILE} \
                                    --version 1.0.${BUILD_NUMBER}\
                                    --namespace scraper \
                                    --set image.tag=${DOCKER_TAG} \
                                    install/helm/backend"
                            }
                            stage('helm upgrade backend-logs') {
                                sh "helm upgrade --install backend-logs \
                                    -f install/helm/backend-logs/values.yaml \
                                    -f install/helm/backend-logs/${VALUES_FILE} \
                                    --version 1.0.${BUILD_NUMBER}\
                                    --namespace scraper \
                                    install/helm/backend-logs"
                            }
                            stage('helm upgrade docs') {
                                sh "helm upgrade --install docs \
                                    -f install/helm/docs/values.yaml \
                                    -f install/helm/docs/${VALUES_FILE} \
                                    --version 1.0.${BUILD_NUMBER}\
                                    --namespace scraper \
                                    install/helm/docs"
                            }
                        }
                    }
                }
            }
        }