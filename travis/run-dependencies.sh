#!/usr/bin/env bash

here=$(dirname $0)

$here/../src/gradlew -p src :generateTestData

docker network create montagu

docker run -d --network=montagu --name db vimc/montagu-db:master
docker run -d --network=montagu --name api -p 8080:8080 vimc/montagu-api:master
docker run -d --network=montagu --volume $PWD/src/app/git:/orderly --user $UID -p 8321:8321 vimc/orderly.server:master /orderly
docker run -d --network=montagu -p 80:80 vimc/montagu-reverse-proxy-minimal:master

docker exec db montagu-wait.sh

export NETWORK=montagu
$here/../scripts/setup-montagu-db.sh

docker exec api mkdir -p /etc/montagu/api
docker exec api touch /etc/montagu/api/go_signal


docker run --rm -v ${TRAVIS_BUILD_DIR}/src/app/demo:/orderly vimc/orderlyweb-migrate:$TRAVIS_BRANCH
docker run --rm -v ${TRAVIS_BUILD_DIR}/src/app/git:/orderly vimc/orderlyweb-migrate:$TRAVIS_BRANCH
