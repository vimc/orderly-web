#!/usr/bin/env bash
set -e

cd $(dirname $0)

config_path=$(realpath ../config)
export MONTAGU_API_VERSION=$(<$config_path/api_version)
export MONTAGU_DB_VERSION=$(<$config_path/db_version)
export MONTAGU_ORDERLY_SERVER_VERSION=$(<$config_path/orderly_server_version)
cert_tool_version=master

export TOKEN_KEY_PATH=/etc/montagu/reports_api/token_key
export MONTAGU_ORDERLY_PATH=$(realpath ../src/app/git)
export ORDERLY_SERVER_USER_ID=$(id -u $USER)

(
	cd ../src
	if [[ ! -d app/demo ]]; then
		./gradlew :generateTestData
	fi
)

# Generate a keypair
docker run --rm \
    -v $TOKEN_KEY_PATH:/workspace \
    docker.montagu.dide.ic.ac.uk:5000/montagu-cert-tool:$cert_tool_version \
    gen-keypair /workspace

docker-compose up -d
docker exec dev_api_1 touch /etc/montagu/api/go_signal

echo "Depedencies are now running; press Ctrl+C to teardown"

# From now on, if the user presses Ctrl+C we should teardown gracefully
trap on_interrupt INT
function on_interrupt() {
	docker kill dev_orderly_server_1
    docker-compose down
}

sleep infinity
