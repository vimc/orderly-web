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

# Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

# Compile, test and package the app
function cleanup() {
  COMPOSE_FILE=$here/../scripts/docker-compose.yml
  docker-compose -f $COMPOSE_FILE  --project-name montagu down

  zip -qr reports.zip reports
  bash <(curl -s https://codecov.io/bash) -s coverage/
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
