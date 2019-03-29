#!/usr/bin/env bash
set -ex

 # Build and image that can run blackbox tests
docker build -f customConfig.Dockerfile \
        -t orderly-web-config-tests \
     	.

 # Run the tests
docker run --rm \
    -v $PWD/demo:/api/src/customConfigTests/demo \
    -v $PWD/git:/api/src/customConfigTests/git \
    orderly-web-config-tests
