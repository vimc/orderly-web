#!/usr/bin/env bash
set -ex

 # Build and image that can run blackbox tests
docker build -f customConfig.Dockerfile \
        -t orderly-web-config-tests \
     	.

 # Run the tests
docker run orderly-web-config-tests
