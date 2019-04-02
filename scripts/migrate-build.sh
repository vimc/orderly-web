#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/migrate-common.sh

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       --tag $COMMIT_TAG_PUBLIC \
       --tag $BRANCH_TAG_PUBLIC \
       -f migrations/Dockerfile \
       .
