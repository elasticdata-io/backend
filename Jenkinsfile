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
  - name: k8s-helm-2
    image: lachlanevenson/k8s-helm:v2.12.3
    command:
    - cat
    tty: true
    volumeMounts:
      - name: kubeconfig
        mountPath: "/opt/.kube"
  - name: kubectl
    image: lachlanevenson/k8s-kubectl:v1.16.0
    command:
    - cat
    tty: true
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
                            }
                            stage('publish application') {
                            }
                            stage('rm application') {

                            }
                        }

                        container('kubectl') {
                            stage('test') {
                                sh "kubectl get nodes"
                            }
                        }
                        container('k8s-helm-2') {
                            stage('SET ENV') {
                                if (env.BRANCH_NAME == 'master') {
                                    env.VALUES_FILE = 'values-prod.yaml'
                                }
                            }
                            stage('helm delete backend') {
                                sh "helm delete --purge backend"
                            }
                            stage('helm delete backend-logs') {
                                sh "helm delete --purge backend-logs"
                            }
                        }
                        container('k8s-helm') {
                            stage('SET ENV') {
                                if (env.BRANCH_NAME == 'master') {
                                    env.VALUES_FILE = 'values-prod.yaml'
                                }
                            }
                            stage('helm upgrade backend') {
                                sh "helm upgrade backend \
                                    -f install/helm/backend/values.yaml \
                                    -f install/helm/backend/${VALUES_FILE} \
                                    --version 1.0.${BUILD_NUMBER}\
                                    --namespace scraper \
                                    install/helm/backend"
                            }
                            stage('helm upgrade backend-logs') {
                                sh "helm upgrade backend-logs \
                                    -f install/helm/backend-logs/values.yaml \
                                    -f install/helm/backend-logs/${VALUES_FILE} \
                                    --version 1.0.${BUILD_NUMBER}\
                                    --namespace scraper \
                                    install/helm/backend-logs"
                            }
                        }
                    }
                }
            }
        }