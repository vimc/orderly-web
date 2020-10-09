#!/usr/bin/env bash
set -ex

BRANCH=$BUILDKITE_BRANCH
COMMIT=$BUILDKITE_COMMIT

REGISTRY=vimc
NAME=orderlyweb-migrate

TAG=$REGISTRY/$NAME
COMMIT_TAG=$REGISTRY/$NAME:$COMMIT
BRANCH_TAG=$REGISTRY/$NAME:$BRANCH

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .
