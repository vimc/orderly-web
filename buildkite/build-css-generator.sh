#!/usr/bin/env bash

set -ex

here=$(dirname $0)
. $here/common

NAME=orderly-web-css-generator

TAG=$REGISTRY/$NAME
COMMIT_TAG=$REGISTRY/$NAME:$GIT_ID
BRANCH_TAG=$REGISTRY/$NAME:$GIT_BRANCH

docker build \
        --tag $COMMIT_TAG \
        --tag $BRANCH_TAG \
        -f css.Dockerfile \
        .

docker push $COMMIT_TAG
docker push $BRANCH_TAG
