#!/usr/bin/env bash
set -e

here=$(dirname $0)
$here/../buildkite/make-db.sh
(
	cd $here/../src
	./gradlew :customConfigTests:copyOrderlyDemo
)

export ORDERLY_DEMO=$(realpath $here/../src/app/demo)

export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

echo "Dependencies are now running; press Ctrl+C to teardown"

# From now on, if the user presses Ctrl+C we should teardown
function cleanup() {
    docker-compose -f $here/../scripts/docker-compose.yml --project-name montagu down
}
trap cleanup INT

sleep infinity
