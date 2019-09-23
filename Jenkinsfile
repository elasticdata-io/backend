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
  - name: tmp
    hostPath:
      path: /tmp/kubectl
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
      - name: tmp
        mountPath: "/tmp/kubectl"
  - name: kubectl
    image: lachlanevenson/k8s-kubectl:v1.16.0
    command:
    - cat
    tty: true
    volumeMounts:
      - name: tmp
        mountPath: "/tmp/kubectl"
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
                                sh "helm template backend install/helm/backend >> /tmp/kubectl/backend.yaml"
                            }
                            stage('helm delete backend-logs') {
                                sh "helm template backend-logs install/helm/backend-logs >> /tmp/kubectl/backend-logs.yaml"
                            }
                        }
                        container('kubectl') {
                            stage('test') {
                                sh "ls -l /opt/kubectl"
                            }
                        }
                    }
                }
            }
        }