#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/common

docker build --tag $BUILD_ENV_TAG .

# We have to push this so it's available to other build steps
docker push $BUILD_ENV_TAG
