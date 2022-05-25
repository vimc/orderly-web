#!/usr/bin/env bash

set -euxo pipefail

here=$(dirname $0)

# Set up environment
. $here/common

# Make the build environment image that is shared between build targets
$here/make-build-env.sh

# Create an image based on the build image to compile, test and package the app
docker build \
    --file app.Dockerfile \
    --tag orderly-web-app-build \
    --build-arg git_id=$GIT_ID \
	  --build-arg git_branch=$GIT_BRANCH \
	  .

# Generate orderly data and migrate for orderly web tables
$here/make-db.sh

# Fix up git remote
git --git-dir=$here/../git/.git remote set-url origin /orderly/upstream

# Run all dependencies
export MONTAGU_ORDERLY_PATH_PARENT=$PWD
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

# Compile, test and package the app
function cleanup() {
  $here/../scripts/clear-docker.sh
  zip -qr reports.zip reports
  $here/codecov.sh -s coverage/
  sudo chown -R $UID reports coverage
}
trap cleanup EXIT
docker run --rm \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    -v $PWD/reports:/api/src/app/build/reports \
    -v $PWD/coverage:/api/src/app/coverage \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $BUILDKITE_DOCKER_AUTH_PATH:/root/.docker/config.json \
    --network=host \
    orderly-web-app-build
