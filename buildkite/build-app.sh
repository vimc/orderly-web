#!/usr/bin/env bash

set -ex
here=$(dirname $0)
. $here/common

# Make the build environment image that is shared between multiple build targets
$here/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag orderly-web-app-build \
	--build-arg git_id=$GIT_ID \
	--build-arg git_branch=$GIT_BRANCH \
    -f app.Dockerfile \
	.

# Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

function cleanup {
    set +e
    docker-compose -f $here/../scripts/docker-compose.yml  --project-name montagu down
}
trap cleanup EXIT

#TODO - pull this out into a script to be called both from here and migrate - even in common?
ORDERLY_IMAGE=$REGISTRY/orderly:master

rm demo -rf
rm git -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    $ORDERLY_IMAGE \
    "."


# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $BUILDKITE_DOCKER_AUTH_PATH:/root/.docker/config.json \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    orderly-web-app-build
