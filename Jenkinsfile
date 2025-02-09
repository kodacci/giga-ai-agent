def PROJECT_VERSION
def DEPLOY_GIT_SCOPE
def CORE_APP_IMAGE_TAG

static def genImageTag(name, scope, version, buildNumber) {
    return 'pro.ra-tech/giga-ai-agent/' +
            scope + '/' + name + ':' +
            version + '-' + buildNumber
}

def buildImage(name, dockerFilePath, scope, version, buildNumber) {
    def tag = genImageTag(name, scope, version, buildNumber)

    docker.withServer(DOCKER_HOST, 'jenkins-client-cert') {
        echo "Building image with tag '$tag'"
        def image = docker.build(tag, '-f ' + dockerFilePath + ' .')

        docker.withRegistry(SNAPSHOTS_DOCKER_REGISTRY_HOST, 'vault-nexus-deployer') {
            image.push()
            image.push('latest')
        }
    }

    return tag
}

pipeline {
    agent { label 'jenkins-agent1' }

    options {
        ansiColor('xterm')
    }

    stages {
        stage('Determine Version') {
            steps {
                script {
                    withMaven(globalMavenSettingsConfig: 'maven-config-ra-tech') {
                        PROJECT_VERSION = sh(
                                encoding: 'UTF-8',
                                returnStdout: true,
                                script: './mvnw help:evaluate "-Dexpression=project.version" -B -Dsytle.color=never -q -DforceStdout'
                        ).trim()
                        DEPLOY_GIT_SCOPE =
                                sh(encoding: 'UTF-8', returnStdout: true, script: 'git name-rev --name-only HEAD')
                                        .trim()
                                        .tokenize('/')
                                        .last()
                                        .toLowerCase()
                        echo "Project version: '${PROJECT_VERSION}'"
                        echo "Git branch scope: '${DEPLOY_GIT_SCOPE}'"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    println("Building project version: " + PROJECT_VERSION)
                    def logFileName = env.BUILD_TAG + '-build.log'
                    try {
                        withMaven(globalMavenSettingsConfig: 'maven-config-ra-tech') {
                            sh "./mvnw --log-file \"$logFileName\" -DskipTests clean package"
                        }
                    } finally {
                        archiveArtifacts(logFileName)
                        sh "rm \"$logFileName\""
                    }
                    println("Build finished")
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    println("Starting build verification")
                    def logFileName = env.BUILD_TAG + '-test.log'
                    try {
                        withMaven(globalMavenSettingsConfig: 'maven-config-ra-tech') {
                            docker.withServer(DOCKER_HOST, 'jenkins-client-cert') {
                                sh "./mvnw --log-file \"$logFileName\" verify"
                            }
                        }
                    } finally {
                        archiveArtifacts(logFileName)
                        sh "rm \"$logFileName\""
                    }
                    println("Verification finished")
                }
            }
        }

        stage('Deploy to Nexus Snapshots') {
            when {
                not {
                    branch 'release/*'
                }
            }

            steps {
                script {
                    def logFileName = env.BUILD_TAG + '-deploy.log'
                    try {
                        withMaven(mavenSettingsConfig: 'maven-config-ra-tech') {
                            sh "./mvnw help:effective-settings"
                            sh "./mvnw -X --log-file \"$logFileName\" deploy -Drevision=$PROJECT_VERSION-$DEPLOY_GIT_SCOPE-SNAPSHOT -DskipTests"
                        }
                    } finally {
                        archiveArtifacts(logFileName)
                        sh "rm \"$logFileName\""
                    }

                    println('Deploying to nexus finished')
                }
            }
        }

        stage('Build core docker image') {
            steps {
                script {
                    CORE_APP_IMAGE_TAG = buildImage(
                            'giga-ai-agent-core',
                            'distrib/docker/core/Dockerfile',
                            DEPLOY_GIT_SCOPE,
                            PROJECT_VERSION,
                            currentBuild.number
                    )
                }
            }
        }

//        stage('Trigger deploy pipeline') {
//            steps {
//                script {
//                    def path = BRANCH_NAME.replaceAll("/", "%2F")
//                    build(
//                            job: "Giga AI Agent Backend CD/$path",
//                            wait: false,
//                            parameters: [
//                                    string(name: 'core_app_image', value: CORE_APP_IMAGE_TAG),
//                            ]
//                    )
//                }
//            }
//        }
    }
}