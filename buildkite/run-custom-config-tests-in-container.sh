#!/usr/bin/env bash
set -ex
here=$(dirname $0)
. $here/common

# Create an image based on the shared build env that runs the tests
docker build --tag orderly-web-custom-config-tests \
    --build-arg GIT_ID=$GIT_ID \
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
    -v $ORDERLY_DEMO:/api/src/app/demo \
    -v $ORDERLY_DEMO:/api/src/customConfigTests/demo \
    --network=host \
    orderly-web-custom-config-tests
