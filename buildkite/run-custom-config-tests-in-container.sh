#!/usr/bin/env bash
set -ex
here=$(dirname $0)

# Make the build environment image that is shared between multiple build targets
$here/make-build-env.sh

# Create an image based on the shared build env that runs the tests
docker build --tag orderly-web-custom-config-tests \
    -f customConfigTests.Dockerfile \
	.

## Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

function cleanup(){
    docker-compose -f $here/docker-compose.yml --project-name montagu down
}
trap cleanup EXIT

export NETWORK=montagu_default

$here/../scripts/montagu-cli.sh add "Test User" test.user \
    test.user@example.com password \

$here/../scripts/montagu-cli.sh addRole test.user user

# Run the created image
docker run --rm \
    -v $PWD/git:/api/src/customConfigTests/git \
    --network=host \
    orderly-web-custom-config-tests
