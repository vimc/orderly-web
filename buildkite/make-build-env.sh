#!/usr/bin/env bash
set -ex


HERE=$(dirname $0)
. $HERE/common

docker build --tag $BUILD_ENV_TAG .
