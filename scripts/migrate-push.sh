#!/usr/bin/env bash
set -e

HERE=$(dirname $0)
. $HERE/migrate-common.sh

# push to the registry
docker push $COMMIT_TAG
docker push $BRANCH_TAG