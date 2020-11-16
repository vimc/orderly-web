#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/common
. $HERE/migrate-common.sh

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \
       .
