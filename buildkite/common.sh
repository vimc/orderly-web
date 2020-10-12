#!/usr/bin/env bash
ORG=vimc

if [ "$BUILDKITE" = "true" ]; then
    GIT_SHA=${BUILDKITE_COMMIT:0:7}
else
    GIT_SHA=$(git rev-parse --short=7 HEAD)
fi

if [ "$BUILDKITE" = "true" ]; then
    GIT_BRANCH=$BUILDKITE_BRANCH
else
    GIT_BRANCH=$(git symbolic-ref --short HEAD)
fi

# Deal with dependabot tags which look like
#
#   dependabot/npm_and_yarn/app/lodash-4.17.19
#
# But docker does not like
GIT_BRANCH=$(echo $GIT_BRANCH | sed 's;/;-;g')

BUILDKITE_DOCKER_AUTH_PATH=/var/lib/buildkite-agent/.docker/config.json
BUILD_ENV_TAG=$ORG/orderly-web-shared-build-env:$GIT_SHA

# Export env vars needed for running test dependencies
export ORDERLY_SERVER_VERSION=$(<$src/config/orderly_server_version)
#export DB_PORT=5432
#export NETWORK=test_nw
export GIT_SHA=$GIT_SHA
export GIT_BRANCH=$GIT_BRANCH
export BUILD_ENV_TAG=orderly-web-build-environment


