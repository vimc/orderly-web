#!/usr/bin/env bash
set -e

cd $(dirname $0)

config_path=$(realpath ../config)
export MONTAGU_API_VERSION=$(<$config_path/api_version)
export MONTAGU_DB_VERSION=$(<$config_path/db_version)
export MONTAGU_ORDERLY_SERVER_VERSION=$(<$config_path/orderly_server_version)

export TOKEN_KEY_PATH=/etc/montagu/reports_api/token_key
export MONTAGU_ORDERLY_PATH=$(realpath ../src/app/git)

echo "using orderly path:"
echo $MONTAGU_ORDERLY_PATH

export ORDERLY_SERVER_USER_ID=$(id -u $USER)

#(
#	cd ../src
#	# get fresh tests data
#	rm app/demo -rf
#	rm app/git -rf
#	./gradlew :generateTestData
#)

docker-compose pull
docker-compose up -d

trap cleanup EXIT

docker exec dev_db_1 montagu-wait.sh

export MIGRATE_IMAGE=docker.montagu.dide.ic.ac.uk:5000/montagu-migrate:${MONTAGU_DB_VERSION}

docker pull ${MIGRATE_IMAGE}
docker run --rm --network=dev_default \
                     ${MIGRATE_IMAGE} \
                     migrate

./cli.sh add "Test User" test.user \
    test.user@example.com password \

./cli.sh addRole test.user user
./cli.sh addRole test.user admin

docker exec dev_api_1 touch /etc/montagu/api/go_signal

echo "Dependencies are now running; press Ctrl+C to teardown"

# From now on, if the user presses Ctrl+C we should teardown gracefully
trap cleanup INT
function cleanup() {
	docker kill dev_orderly_server_1
    docker-compose down
}

sleep infinity
