#!/usr/bin/env bash
set -ex

HERE=$(dirname $0)
. $HERE/common

docker build --tag orderly-web-build-environment .
