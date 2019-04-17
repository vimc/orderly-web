#!/usr/bin/env bash

set -ex
here=$(dirname $0)

git_id=$(git rev-parse --short=7 HEAD)
git_branch=$(git symbolic-ref --short HEAD)

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that runs the tests
docker build --tag orderly-web-custom-config-tests \
    -f customConfigTests.Dockerfile \
	.

## Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/run-dependencies.sh

function cleanup(){
    docker-compose -f $here/docker-compose.yml --project-name montagu down
}
trap cleanup EXIT

export NETWORK=montagu_default

$here/migrate-test.sh

# Run the created image
docker run --rm \
    -v $PWD/demo:/api/src/customConfigTests/demo \
    --network=host \
    orderly-web-custom-config-tests
