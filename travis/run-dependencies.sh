#!/usr/bin/env bash

here=$(dirname $0)

$here/../src/gradlew -p src :generateTestData

docker network create montagu

docker run -d --network=montagu --name db -p 5432:5432 vimc/montagu-db:master
docker run -d --network=montagu --name api -p 8080:8080 vimc/montagu-api:master
docker run -d --network=montagu --volume $PWD/src/app/git:/orderly --user $UID -p 8321:8321 vimc/orderly.server:master /orderly
docker run -d --network=montagu -p 80:80 vimc/montagu-reverse-proxy-minimal:master

docker exec db montagu-wait.sh

docker run --rm --network=montagu vimc/montagu-migrate:master migrate
docker run --network=montagu vimc/montagu-cli:master add "Test User" test.user \
    test.user@example.com password

docker run --network=montagu vimc/montagu-cli:master addRole test.user user

docker run --network=montagu vimc/montagu-cli:master addRole test.user admin

docker exec api mkdir -p /etc/montagu/api
docker exec api touch /etc/montagu/api/go_signal
