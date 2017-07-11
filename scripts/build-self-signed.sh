#!/usr/bin/env bash
set -e
git_id=$(git rev-parse --short HEAD)
git_branch=$(git symbolic-ref --short HEAD)
registry=docker.montagu.dide.ic.ac.uk:5000
name=montagu-generate-self-signed-cert

branch_tag=$registry/$name:$git_branch
commit_tag=$registry/$name:$git_id

docker build \
    -t $commit_tag \
    -f self-signed-cert.Dockerfile \
    .
docker tag $commit_tag $branch_tag
docker push $commit_tag