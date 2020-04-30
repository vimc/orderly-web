#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/common
. $HERE/migrate-common.sh

# push to the registry
docker push $COMMIT_TAG
docker push $BRANCH_TAG
docker push $COMMIT_TAG_PUBLIC
docker push $BRANCH_TAG_PUBLIC
