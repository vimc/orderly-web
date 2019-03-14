#!/usr/bin/env bash
set -ex

TESTING_MIGRATE_IMAGE=orderlyweb_migrate

GIT_ID=$(git rev-parse --short=7 HEAD)
GIT_BRANCH=$(git symbolic-ref --short HEAD)
REGISTRY=docker.montagu.dide.ic.ac.uk:5000
NAME=orderlyweb-migrate

TAG=$REGISTRY/$NAME
COMMIT_TAG=$REGISTRY/$NAME:$GIT_ID
BRANCH_TAG=$REGISTRY/$NAME:$GIT_BRANCH
DB=$REGISTRY/montagu-db:$GIT_ID

# Test migrations by running container before pushing image
docker build --tag $TESTING_MIGRATE_IMAGE -f migrations/Dockerfile .

docker run --rm -v ${PWD}/demo:/orderly $TESTING_MIGRATE_IMAGE

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

# push to the registry
docker push $COMMIT_TAG
docker push $BRANCH_TAG

