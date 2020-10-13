#!/usr/bin/env bash
set -ex

# This script combines building, testing and pushing the migrate image for running in a buildkite step

here=$(dirname $0)
. $here/common

NAME=orderlyweb-migrate
COMMIT_TAG=$REGISTRY/$NAME:$GIT_ID
BRANCH_TAG=$REGISTRY/$NAME:$GIT_BRANCH

docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

ORDERLY_IMAGE=$REGISTRY/orderly:master

rm demo -rf
rm git -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    $ORDERLY_IMAGE \
    "."

docker run --rm -v ${PWD}/demo:/orderly $COMMIT_TAG
docker run --rm -v ${PWD}/git:/orderly $COMMIT_TAG

docker push $COMMIT_TAG
docker push $BRANCH_TAG

