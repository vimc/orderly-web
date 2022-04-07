#!/usr/bin/env bash

git_id=$(git rev-parse --short=7 HEAD)
image=vimc/orderly-web-user-cli:${git_id}
docker run -v $PWD/git:/orderly $image "$@"
