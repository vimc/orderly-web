#!/usr/bin/env bash

image=docker.montagu.dide.ic.ac.uk:5000/montagu-cli:master
exec docker run --network montagu_proxy $image "$@"
