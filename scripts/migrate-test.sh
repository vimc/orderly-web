#!/usr/bin/env bash
set -e

HERE=$(dirname $0)
. $HERE/migrate-common.sh

#rm ${PWD}/demo -rf
#rm ${PWD}/git -rf
#
#ORDERLY_IMAGE=vimc/orderly:master
#docker pull $ORDERLY_IMAGE
#docker run --rm \
#  --entrypoint create_orderly_demo.sh \
#  -u $UID \
#  -v ${PWD}:/orderly \
#  -w /orderly \
#  $ORDERLY_IMAGE \
#  "."

docker run --rm -v ${PWD}/demo:/orderly $COMMIT_TAG
docker run --rm -v ${PWD}/git:/orderly $COMMIT_TAG