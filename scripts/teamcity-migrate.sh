#!/usr/bin/env bash
set -ex

GIT_ID=$(git rev-parse --short=7 HEAD)
GIT_BRANCH=$(git symbolic-ref --short HEAD)
REGISTRY=docker.montagu.dide.ic.ac.uk:5000
NAME=orderlyweb-migrate

TAG=$REGISTRY/$NAME
COMMIT_TAG=$REGISTRY/$NAME:$GIT_ID
BRANCH_TAG=$REGISTRY/$NAME:$GIT_BRANCH
DB=$REGISTRY/montagu-db:$GIT_ID

## Get directory of the 'scripts/' directory
DIR=$(dirname "$(readlink -f "$0")")

docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

# run this first to avoid a spurious pull error message
docker push $COMMIT_TAG
docker push $BRANCH_TAG

# TODO: should test can run the image here, alongside an orderly image to initially generate the db?