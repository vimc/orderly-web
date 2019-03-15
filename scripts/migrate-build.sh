#!/usr/bin/env bash
set -e

HERE=$(dirname $0)
. $HERE/migrate-common.sh

# build tagged images
docker build \
       --tag $COMMIT_TAG \
       --tag $BRANCH_TAG \
       -f migrations/Dockerfile \.