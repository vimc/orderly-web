#!/usr/bin/env bash

set -ex
docker pull vimc/orderly.server:master
docker pull vimc/montagu-reverse-proxy-minimal:master
docker pull vimc/orderlyweb-migrate:master
docker pull vimc/montagu-api:master
docker pull vimc/montagu-db:master
docker pull vimc/montagu-migrate:master
docker pull vimc/montagu-cli:master
