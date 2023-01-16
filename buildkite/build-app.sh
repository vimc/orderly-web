#!/usr/bin/env bash

set -euxo pipefail

here=$(dirname $0)

# Set up environment
. $here/common

# Create an image based on the build image to compile, test and package the app
docker build \
    --file app.Dockerfile \
    --tag orderly-web-app-build \
    --build-arg GIT_ID=$GIT_ID \
	  --build-arg GIT_BRANCH=$GIT_BRANCH \
	  .

# Generate orderly data and migrate for orderly web tables
$here/make-db.sh

# Run all dependencies
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
    -v $ORDERLY_DEMO:/api/src/app/demo \
    -v $OUTPACK_DEMO:/api/src/app/outpack \
    -v $PWD/reports:/api/src/app/build/reports \
    -v $PWD/coverage:/api/src/app/coverage \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $BUILDKITE_DOCKER_AUTH_PATH:/root/.docker/config.json \
    --network=host \
    orderly-web-app-build
