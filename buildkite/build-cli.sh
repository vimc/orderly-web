#!/usr/bin/env bash
set -ex

here=$(dirname $0)
. $here/common

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

# Generate orderly data and migrate for orderly web tables
$here/make-db.sh

# Run the created image
function cleanup() {
  $here/../scripts/clear-docker.sh
}
trap cleanup EXIT
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $BUILDKITE_DOCKER_AUTH_PATH:/root/.docker/config.json \
    -v $ORDERLY_DEMO:/api/src/userCLI/demo \
    --network=host \
    orderly-web-cli-build

