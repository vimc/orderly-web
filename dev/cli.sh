#!/usr/bin/env bash

git_id=$(git rev-parse --short=7 HEAD)

image=docker.montagu.dide.ic.ac.uk:5000/orderly-web-cli:$git_id
docker run -v $PWD/src/app/demo:/orderly $image "$@"
