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

# Test migrations before pushing image
docker build --tag $TESTING_MIGRATE_IMAGE -f migrations/Dockerfile .

docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker run --rm --entrypoint create_orderly_demo.sh \
    -u ${UID} \
    -v ${PWD}:/orderly \
    -w /orderly \
    docker.montagu.dide.ic.ac.uk:5000/orderly:master \
    "./src/app/"

# Do the test migration
docker run --rm -v ${PWD}/src/app/demo:/orderly $TESTING_MIGRATE_IMAGE

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

# push to the registry
docker push $COMMIT_TAG
docker push $BRANCH_TAG

