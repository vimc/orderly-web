#!/usr/bin/env bash

set -euxo pipefail

here=$(dirname $0)

# Set up environment
. $here/common

docker build \
    --file standalone.Dockerfile \
    --tag orderly-web-standalone:$GIT_ID \
    --build-arg GIT_ID=$GIT_ID \
    .
