#!/usr/bin/env bash
set -e

here=$(dirname $0)

config_path=$(realpath $here/../config)
export MONTAGU_API_VERSION=$(<$config_path/api_version)
export MONTAGU_DB_VERSION=$(<$config_path/db_version)
export MONTAGU_ORDERLY_SERVER_VERSION=$(<$config_path/orderly_server_version)

export MONTAGU_ORDERLY_PATH=$(realpath $here/../src/app/git)

export ORDERLY_SERVER_USER_ID=$(id -u $USER)

(
	cd $here/../src
	# get fresh tests data
	rm app/demo -rf
	rm app/git -rf
	./gradlew :generateTestData
)

$here/../scripts/run-dependencies.sh

echo "Dependencies are now running; press Ctrl+C to teardown"

# From now on, if the user presses Ctrl+C we should teardown
function cleanup() {
    docker-compose -f $here/../scripts/docker-compose.yml --project-name montagu down
}
trap cleanup INT
trap cleanup EXIT

sleep infinity
