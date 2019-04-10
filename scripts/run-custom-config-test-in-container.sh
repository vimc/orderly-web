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

# Run all dependencies
git clone https://github.com/vimc/montagu --recursive

function cleanup() {
    rm montagu -rf
    $here/clear-docker.sh
}

trap cleanup EXIT

( cd montagu && \
    cp settings/teamcity.json src/montagu-deploy.json && \
    pip3 install --quiet -r src/requirements.txt && \
    ./src/deploy.py
)
export NETWORK=montagu_default

$here/montagu-cli.sh add "Test User" test.user \
    test.user@example.com password \

$here/montagu-cli.sh addRole test.user user

$here/migrate-test.sh

# Run the created image
docker run --rm \
    -v $PWD/demo:/api/src/customConfigTests/demo \
    --network=host \
    orderly-web-custom-config-tests

( cd montagu && ./src/stop.py )
