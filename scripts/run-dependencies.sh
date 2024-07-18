#!/usr/bin/env bash
here=$(dirname $0)

echo "using orderly path:"
echo $ORDERLY_DEMO

# Fix up git remote
git --git-dir=$ORDERLY_DEMO/.git remote set-url origin /orderly/upstream

config_path=$(realpath $here/../config)
export ORDERLY_DEMO=$(realpath $ORDERLY_DEMO)
export OUTPACK_DEMO=$(realpath $here/../src/app/outpack)
export MONTAGU_ORDERLY_SERVER_VERSION=$(<$config_path/orderly_server_version)

mkdir -p $OUTPACK_DEMO
docker pull mrcide/outpack.orderly:main
docker run -v $ORDERLY_DEMO:/orderly:ro -v $OUTPACK_DEMO:/outpack -u $UID mrcide/outpack.orderly:main /orderly /outpack --once

COMPOSE_FILE=$here/../scripts/docker-compose.yml

docker compose --compatibility -f $COMPOSE_FILE pull || true
docker compose --compatibility -f $COMPOSE_FILE --project-name montagu up -d

function cleanup() {
    docker compose --compatibility -f $COMPOSE_FILE  --project-name montagu down
}

trap cleanup ERR

# This is sometimes necessary locally, to give db time to start
# sleep 20

docker exec montagu_db_1 montagu-wait.sh 120

export NETWORK=montagu_default

$here/../scripts/setup-montagu-db.sh

docker exec montagu_api_1 mkdir -p /etc/montagu/api
docker exec montagu_api_1 touch /etc/montagu/api/go_signal

$here/../scripts/montagu-cli.sh add "Test User" test.user \
    test.user@example.com password \

$here/../scripts/montagu-cli.sh addRole test.user user
