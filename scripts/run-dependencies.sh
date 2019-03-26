#!/usr/bin/env bash
here=$(dirname $0)

echo "using orderly path:"
echo $MONTAGU_ORDERLY_PATH

config_path=$(realpath $here/../config)
export MONTAGU_API_VERSION=$(<$config_path/api_version)
export MONTAGU_DB_VERSION=$(<$config_path/db_version)
export MONTAGU_ORDERLY_SERVER_VERSION=$(<$config_path/orderly_server_version)

COMPOSE_FILE=$here/../scripts/docker-compose.yml

docker-compose -f $COMPOSE_FILE pull
docker-compose -f $COMPOSE_FILE --project-name montagu up -d

function cleanup() {
    docker-compose -f $COMPOSE_FILE  --project-name montagu down
}

trap cleanup ERR

docker exec montagu_db_1 montagu-wait.sh

export NETWORK=montagu_default

$here/../scripts/setup-montagu-db.sh

docker exec montagu_api_1 mkdir -p /etc/montagu/api
docker exec montagu_api_1 touch /etc/montagu/api/go_signal
