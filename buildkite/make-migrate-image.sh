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

## ORDERLY_SERVER_VERSION can be set as an env var to build
## orderly-web with a specific version of orderly.server. We
## do this on triggered build to test how changes to
## orderly.server will affect OW. If the orderly.server
## version is not the default branch then use a
## combined tag so that we never replace an existing tagged
## image built off master with an image with a different
## version of orderly installed
ORDERLY_SERVER_VERSION="${ORDERLY_SERVER_VERSION:-master}"
if [ "$ORDERLY_SERVER_VERSION" != "master" ]; then
       COMMIT_TAG="$COMMIT_TAG-$ORDERLY_SERVER_VERSION"
       BRANCH_TAG="$BRANCH_TAG-$ORDERLY_SERVER_VERSION"
fi

docker push $COMMIT_TAG
docker push $BRANCH_TAG
