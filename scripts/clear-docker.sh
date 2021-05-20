#!/usr/bin/env bash

set -euxo pipefail

docker rm --force $(docker ps --all --quiet) || true
docker network prune --force
docker volume prune --force
