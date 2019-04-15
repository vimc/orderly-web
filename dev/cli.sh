#!/usr/bin/env bash

image=docker.montagu.dide.ic.ac.uk:5000/orderly-web-cli:master
docker run -v $PWD/src/app/demo:/orderly $image "$@"
