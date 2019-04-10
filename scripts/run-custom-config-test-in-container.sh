#!/usr/bin/env bash

set -ex
here=$(dirname $0)

git_id=$(git rev-parse --short=7 HEAD)
git_branch=$(git symbolic-ref --short HEAD)

# Create an image based on the shared build env that runs the tests
docker build --tag orderly-web-custom-config-tests \
    -f customConfigTests.Dockerfile \
	.

# Run all dependencies
git clone https://github.com/vimc/montagu-proxy

function cleanup() {
    rm montagu-proxy -rf
}

trap cleanup EXIT

( cd montagu-proxy && nohup ./scripts/dev.sh ) &

#pid=$!

# Run the created image
docker run --rm \
    -v $PWD/demo:/api/src/customConfigTests/demo \
    --network=host \
    orderly-web-custom-config-tests

##kill -9 $pid
