#!/usr/bin/env bash
set -ex

MIGRATE_IMAGE=orderlyweb_migrate
NETWORK=orderly_db_nw
ORDERLY_SERVER_VERSION=$(<./config/orderly_server_version)

docker build --tag $MIGRATE_IMAGE -f migrations/Dockerfile .

./scripts/create-orderly-demo.sh

# Do the migrations
docker run --rm --network=$NETWORK $MIGRATE_IMAGE