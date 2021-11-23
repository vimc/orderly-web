#!/usr/bin/env bash

ORDERLY_IMAGE=vimc/orderly:vimc-4977

rm demo -rf
rm git -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    $ORDERLY_IMAGE \
    "."

MIGRATE_IMAGE=vimc/orderlyweb-migrate:master

docker pull $MIGRATE_IMAGE

docker run --rm \
    -v $PWD/demo:/orderly \
    $MIGRATE_IMAGE

docker run --rm \
    -v $PWD/git:/orderly \
    $MIGRATE_IMAGE
