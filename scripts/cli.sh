#!/usr/bin/env bash

image=docker.montagu.dide.ic.ac.uk:5000/montagu-cli:$MONTAGU_API_VERSION
exec docker run --network $NETWORK $image "$@"
