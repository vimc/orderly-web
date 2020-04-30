#!/usr/bin/env bash

set -ex
here=$(dirname $0)
. $here/common

# This is the path for teamcity agents. If running locally, pass in your own docker config location
# i.e. /home/{user}/.docker/config.json
docker_auth_path=${1:-/opt/teamcity-agent/.docker/config.json}

# Make the build environment image that is shared between multiple build targets
$here/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag orderly-web-app-build \
	--build-arg git_id=$GIT_ID \
	--build-arg git_branch=$GIT_BRANCH \
    -f app.Dockerfile \
	.

# Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/run-dependencies.sh

function cleanup {
    set +e
    docker-compose -f $here/docker-compose.yml  --project-name montagu down
}
trap cleanup EXIT

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $docker_auth_path:/root/.docker/config.json \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    orderly-web-app-build
