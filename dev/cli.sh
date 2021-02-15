#!/usr/bin/env bash

image=vimc/orderly-web-user-cli:master
# docker run -v $PWD/src/app/demo:/orderly $image "$@"
docker run -v $PWD/src/app/git:/orderly $image "$@"
