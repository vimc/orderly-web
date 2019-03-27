#!/usr/bin/env bash
set -ex

git_id=$(git rev-parse --short=7 HEAD)

# Build and image that can run blackbox tests
docker build -f blackbox.Dockerfile \
        -t orderly-web-blackbox-tests \
     	--build-arg git_id=$git_id \
     	.

# Run the tests
docker run -v /var/run/docker.sock:/var/run/docker.sock orderly-web-blackbox-tests
