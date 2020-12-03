#!/usr/bin/env bash

set -ex
here=$(dirname $0)
. $here/common

# Make the build environment image that is shared between multiple build targets
$here/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag orderly-web-app-build \
	--build-arg git_id=$GIT_ID \
	--build-arg git_branch=$GIT_BRANCH \
    -f app.Dockerfile \
	.

# Generate orderly data and migrate for orderly web tables
$here/make-db.sh

# Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $BUILDKITE_DOCKER_AUTH_PATH:/root/.docker/config.json \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    -v $PWD/reports:/api/src/app/build/reports \
    --network=host \
    orderly-web-app-build

# Persist the test reports
buildkite-agent artifact upload "reports/**/*"
