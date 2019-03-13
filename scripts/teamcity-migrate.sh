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

docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

# run this first to avoid a spurious pull error message
docker push $COMMIT_TAG
docker push $BRANCH_TAG

# test if we can run the migrations using the registry image - the following is otherwise the same as test-migrate
docker pull $COMMIT_TAG

docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker run --rm --entrypoint create_orderly_demo.sh \
    -u ${UID} \
    -v ${PWD}:/orderly \
    -w /orderly \
    docker.montagu.dide.ic.ac.uk:5000/orderly:master \
    "./src/app/"

# Do the migrations
#docker run --rm -v ${PWD}/src/app/demo:/orderly $COMMIT_TAG