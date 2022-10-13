#!/usr/bin/env bash
set -ex

here=$(dirname $0)
. $here/common

# Create an image based on the shared build env that compiles and dockerises
# the CLI, and pushes the image
docker build --tag orderly-web-cli-build \
    --build-arg GIT_ID=$GIT_ID \
    --build-arg GIT_BRANCH=$GIT_BRANCH \
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

