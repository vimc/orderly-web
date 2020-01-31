#!/usr/bin/env bash
# Run this from top level orderly-web dir to test if the orderlyweb migration (adding the orderlyweb schema) can be done
# on an orderly db

set -ex

HERE=$(dirname $0)
. $HERE/migrate-common.sh

if [[ -d demo ]]
then
  echo "Orderly demo folder already exists, not re-creating it"
else
  docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master | true

  docker run --rm --entrypoint create_orderly_demo.sh \
      -u ${UID} \
      -v ${PWD}:/orderly \
      -w /orderly \
      docker.montagu.dide.ic.ac.uk:5000/orderly:master \
      "./src/app/"
fi

# Build the migration image
./scripts/migrate-build.sh

# Do the migrations
docker run --rm -v ${PWD}/src/app/demo:/orderly $COMMIT_TAG
