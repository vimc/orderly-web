#!/usr/bin/env bash
local_path=$1
container=$2

docker exec $container mkdir -p /etc/montagu/api
docker cp $local_path $container:/etc/montagu/api/keystore
rm $local_path