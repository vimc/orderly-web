#!/usr/bin/env bash

set -ex

here=$(dirname $0)
. $here/common

NAME=orderly-web-css-generator

TAG=$ORG/$NAME
COMMIT_TAG=$ORG/$NAME:$GIT_ID
BRANCH_TAG=$ORG/$NAME:$GIT_BRANCH

docker build \
        --tag $COMMIT_TAG \
        --tag $BRANCH_TAG \
        -f css.Dockerfile \
        .

docker push $COMMIT_TAG
docker push $BRANCH_TAG
