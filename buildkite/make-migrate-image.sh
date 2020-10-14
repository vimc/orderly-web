#!/usr/bin/env bash
set -ex

# This script combines building, testing and pushing the migrate image for running in a buildkite step

here=$(dirname $0)
. $here/common

NAME=orderlyweb-migrate
COMMIT_TAG=$ORG/$NAME:$GIT_ID
BRANCH_TAG=$ORG/$NAME:$GIT_BRANCH

docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .

$here//make-db.sh

docker push $COMMIT_TAG
docker push $BRANCH_TAG

