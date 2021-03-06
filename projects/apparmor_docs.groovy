freeStyleJob('apparmor_docs') {
    displayName('apparmor-docs')
    description('Build and update the apparmor docs in jessfraz/apparmor-docs repo.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/apparmor-docs')
    }

    logRotator {
        numToKeep(2)
        daysToKeep(2)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/apparmor-docs.git')
                name('origin')
                credentials('ssh-github-key')
            }
            branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H 1,7,15,21 * *')
    }

    wrappers { colorizeOutput() }

    // unset trust so we can run the image locally
    environmentVariables(DOCKER_CONTENT_TRUST: '0')
    steps {
        shell('if [ ! -f /usr/bin/make ] ; then docker exec -u root jenkins apk add --no-cache make; fi')
        shell('make')

        shell('git diff-index --quiet HEAD || git add . && git commit -am "Update docs"')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        git {
            branch('origin', 'master')
            pushOnlyIfSuccess()
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
