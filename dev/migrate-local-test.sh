#!/usr/bin/env bash
# Run this from top level orderly-web dir to test if the orderlyweb migration (adding the orderlyweb schema) can be done
# on an orderly db

set -ex

HERE=$(dirname $0)
. $HERE/../scripts/common
. $HERE/../scripts/migrate-common.sh

if [[ -d ${HERE}/../src/app/demo ]]
then
  echo "Orderly demo folder already exists, not re-creating it"
else
  docker pull vimc/orderly:master | true

  docker run --rm --entrypoint create_orderly_demo.sh \
      -u ${UID} \
      -v ${PWD}:/orderly \
      -w /orderly \
      vimc/orderly:master \
      "./src/app/"
fi

# Build the migration image
$HERE/../scripts/migrate-build.sh

# Do the migrations
docker run --rm -v ${PWD}/src/app/demo:/orderly $COMMIT_TAG
