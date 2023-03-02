#!/usr/bin/env bash

set -euxo pipefail

here=$(dirname $0)

# Set up environment
. $here/common

BRANCH_TAG=vimc/orderly-web-standalone:$GIT_BRANCH
COMMIT_TAG=vimc/orderly-web-standalone:$GIT_ID

docker build \
    --file standalone.Dockerfile \
    --tag $COMMIT_TAG \
    --build-arg GIT_ID=$GIT_ID \
    .

docker tag $COMMIT_TAG $BRANCH_TAG
docker push $COMMIT_TAG
docker push $BRANCH_TAG
