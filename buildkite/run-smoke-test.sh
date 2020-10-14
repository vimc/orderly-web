#!/usr/bin/env bash

set -ex
here=$(dirname $0)
. $here/common

# create the db
$here/make-db.sh

## Run all dependencies
$here/../scripts/run-dependencies.sh

# Run the OrderlyWeb image
IMAGE=$ORG/orderly-web:$GIT_ID
docker pull $IMAGE
docker run --rm \
    -d \
    -v $PWD/demo:/orderly \
    -p 8888:8888 \
    --name orderly-web \
    $IMAGE

docker exec orderly-web mkdir -p /etc/orderly/web
docker exec orderly-web touch /etc/orderly/web/go_signal

# Wait for go signal
sleep 3

 # Hit the index page and check it works
response=$(curl --write-out %{http_code} --silent --output /dev/null http://localhost:8888/api/v1)

if [[ $response -ne 200 ]]; then exit 1; fi;
