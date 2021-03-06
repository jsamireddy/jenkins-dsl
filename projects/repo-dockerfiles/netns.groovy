freeStyleJob('netns') {
    displayName('netns')
    description('Build Dockerfiles in jessfraz/netns.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/netns')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/netns', 'Docker Hub: jess/netns', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/netns.git')
            }
branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/netns:latest .')
        shell('docker tag r.j3ss.co/netns:latest jess/netns:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/netns:latest')
        shell('docker push --disable-content-trust=false jess/netns:latest')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
