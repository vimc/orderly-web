#!/usr/bin/env bash

image=vimc/montagu-cli:master
exec docker run --network $NETWORK $image "$@"
