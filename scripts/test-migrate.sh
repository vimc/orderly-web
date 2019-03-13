#!/usr/bin/env bash
# Run this from top level orderly-web dir to test if the orderlyweb migration (adding the orderlyweb schema) can be done
# on an orderly db

set -ex

MIGRATE_IMAGE=orderlyweb_migrate
NETWORK=orderly_db_nw
ORDERLY_SERVER_VERSION=$(<./config/orderly_server_version)

# Create orderly demo data just so we're sure of having something to migrate against
# This will fail if there's anything in the demo folder already
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker run --rm --entrypoint create_orderly_demo.sh \
    -u ${UID} \
    -v ${PWD}:/orderly \
    -w /orderly \
    docker.montagu.dide.ic.ac.uk:5000/orderly:master \
    "./src/app/"

# Build the migration image
docker build --tag $MIGRATE_IMAGE -f migrations/Dockerfile .

# Do the migrations
docker run --rm -v ${PWD}/src/app/demo:/orderly $MIGRATE_IMAGE

