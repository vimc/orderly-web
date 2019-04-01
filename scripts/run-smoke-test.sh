#!/usr/bin/env bash

set -ex
here=$(dirname $0)

git_id=$(git rev-parse --short=7 HEAD)
git_branch=$(git symbolic-ref --short HEAD)

## Run all dependencies
export MONTAGU_ORDERLY_PATH=$PWD/git
export ORDERLY_SERVER_USER_ID=$UID
$here/run-dependencies.sh

# Run the OrderlyWeb image
docker run --rm \
    -d \
    -v $PWD/demo:/orderly \
    -p 8081:8081 \
    --name orderly-web \
    docker.montagu.dide.ic.ac.uk:5000/orderly-web:$git_id

function cleanup(){
    docker stop orderly-web
    docker-compose -f $here/docker-compose.yml --project-name montagu down
}
trap cleanup EXIT

docker exec orderly-web mkdir -p /etc/orderly/web
docker exec orderly-web touch /etc/orderly/web/go_signal

# Wait for go signal
sleep 2

 # Hit the index page and check it works
response=$(curl --write-out %{http_code} --silent --output /dev/null http://localhost:8081/api/v1)

if [[ $response -ne 200 ]]; then exit 1; fi;
