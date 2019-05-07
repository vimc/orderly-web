#!/usr/bin/env bash
set -e

HERE=$(dirname $0)
. $HERE/migrate-common.sh

docker run --rm -v ${PWD}/demo:/orderly $COMMIT_TAG
docker run --rm -v ${PWD}/git:/orderly $COMMIT_TAG