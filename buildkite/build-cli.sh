#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/common

# This is the path for teamcity agents. If running locally, pass in your own docker config location
# i.e. /home/{user}/.docker/config.json
#docker_auth_path=${1:-/opt/teamcity-agent/.docker/config.json}

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles and dockerises
# the CLI, and pushes the image
docker build --tag orderly-web-cli-build \
    --build-arg git_id=$GIT_ID \
    --build-arg git_branch=$GIT_BRANCH \
    -f cli.Dockerfile \
    .

# TODO test?

# Migrate the test db
#./scripts/migrate-test.sh

# Run the created image
#docker run --rm \
#    -v /var/run/docker.sock:/var/run/docker.sock \
#    -v $docker_auth_path:/root/.docker/config.json \
#    -v $PWD/demo:/api/src/userCLI/demo \
#    --network=host \
#    orderly-web-cli-build

