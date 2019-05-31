#!/usr/bin/env bash

set -ex
here=$(dirname $0)

GIT_ID=$(git rev-parse --short=7 HEAD)
GIT_BRANCH=$(git symbolic-ref --short HEAD)

REGISTRY=docker.montagu.dide.ic.ac.uk:5000
REGISTRY_PUBLIC=vimc
NAME=orderly-web-css-generator

TAG=$REGISTRY/$NAME
COMMIT_TAG=$REGISTRY/$NAME:$GIT_ID
BRANCH_TAG=$REGISTRY/$NAME:$GIT_BRANCH

COMMIT_TAG_PUBLIC=$REGISTRY_PUBLIC/$NAME:$GIT_ID
BRANCH_TAG_PUBLIC=$REGISTRY_PUBLIC/$NAME:$GIT_BRANCH

# This is the path for teamcity agents. If running locally, pass in your own docker config location
# i.e. /home/{user}/.docker/config.json
docker_auth_path=${1:-/opt/teamcity-agent/.docker/config.json}

docker build \
        --tag $COMMIT_TAG \
        --tag $BRANCH_TAG \
        --tag $COMMIT_TAG_PUBLIC \
        --tag $BRANCH_TAG_PUBLIC \
        -f css.Dockerfile \
        .

docker push $COMMIT_TAG
docker push $BRANCH_TAG
docker push $COMMIT_TAG_PUBLIC
docker push $BRANCH_TAG_PUBLIC
