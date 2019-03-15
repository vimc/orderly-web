#!/usr/bin/env bash

set -ex
git_id=$(git rev-parse --short=7 HEAD)
git_branch=$(git symbolic-ref --short HEAD)
export ORDERLY_SERVER_VERSION=$(<./config/orderly_server_version)

# This is the path for teamcity agents. If running locally, pass in your own docker config location
# i.e. /home/{user}/.docker/config.json
docker_auth_path=${1:-/opt/teamcity-agent/.docker/config.json}

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag orderly-web-app-build \
	--build-arg git_id=$git_id \
	--build-arg git_branch=$git_branch \
    -f app.Dockerfile \
	.

# Run the orderly server
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION

docker run --rm \
    -p 8321:8321 \
    -d \
    -v $PWD/git:/orderly \
    --network=host \
    docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION "orderly"

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $docker_auth_path:/root/.docker/config.json \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    orderly-web-app-build
