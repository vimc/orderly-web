#!/usr/bin/env bash
set -ex
here=$(dirname $0)
. $here/common

# Make the build environment image that is shared between multiple build targets
$here/make-build-env.sh

# Create an image based on the shared build env that runs the tests
docker build --tag orderly-web-custom-config-tests \
    -f customConfigTests.Dockerfile \
	.

# create the db
$here/make-db.sh

## Run all dependencies
$here/../scripts/run-dependencies.sh

# Run the created image
function cleanup() {
  $here/../scripts/clear-docker.sh
}
trap cleanup EXIT
docker run --rm \
    -v $PWD/git:/api/src/customConfigTests/git \
    --network=host \
    orderly-web-custom-config-tests
