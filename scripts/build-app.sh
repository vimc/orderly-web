#!/usr/bin/env bash

set -ex
git_id=$(git rev-parse --short=7 HEAD)
git_branch=$(git symbolic-ref --short HEAD)
export ORDERLY_SERVER_VERSION=$(<./config/orderly_server_version)
export MONTAGU_API_VERSION=$(<./config/api_version)
export MONTAGU_DB_VERSION=$(<./config/db_version)

# This is the path for teamcity agents. If running locally, pass in your own docker config location
# i.e. /home/{user}/.docker/config.json
docker_auth_path=${1:-/opt/teamcity-agent/.docker/config.json}

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag orderly-web-app-build \
	--build-arg git_id=$git_id \
	--build-arg git_branch=$git_branch \
    -f app.Dockerfile \
	.

# Run the orderly server
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION

docker run --rm \
    -d \
    -v $PWD/git:/orderly \
    --network=host \
    docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION "orderly"

# Run the db and migrate
export NETWORK=db_nw

docker network create $NETWORK

function cleanup {
    set +e
    docker stop db api
    docker network rm $NETWORK
}
trap cleanup EXIT

docker run --rm \
    -d \
    --network=$NETWORK \
    -p 5432:5432 \
    --name db \
    docker.montagu.dide.ic.ac.uk:5000/montagu-db:${MONTAGU_DB_VERSION}

docker exec db montagu-wait.sh

MIGRATE_IMAGE=docker.montagu.dide.ic.ac.uk:5000/montagu-migrate:${MONTAGU_DB_VERSION}

docker pull ${MIGRATE_IMAGE}
docker run --rm \
    -d \
    --network=$NETWORK \
    ${MIGRATE_IMAGE} \
    migrate

# Run the api
docker run --rm \
    -d \
    --network=$NETWORK \
    -p 8080:8080 \
    --name api \
    docker.montagu.dide.ic.ac.uk:5000/montagu-api:${MONTAGU_API_VERSION}

docker exec api mkdir -p /etc/montagu/api
docker exec api touch /etc/montagu/api/go_signal

./scripts/cli.sh add "Test User" test.user \
    test.user@example.com password \

./scripts/cli.sh addRole test.user user
./scripts/cli.sh addRole test.user admin

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $docker_auth_path:/root/.docker/config.json \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    orderly-web-app-build
