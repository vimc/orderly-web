set -ex

ORDERLY_IMAGE=$ORG/orderly:master
MIGRATE_IMAGE=$ORG/orderlyweb-migrate:$GIT_ID

rm demo -rf
rm git -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    --env "HOME=/tmp" \
    $ORDERLY_IMAGE \
    "."

docker run --rm -v ${PWD}/demo:/orderly $MIGRATE_IMAGE
docker run --rm -v ${PWD}/git:/orderly $MIGRATE_IMAGE

# Fix up git remote
git --git-dir=$here/../git/.git remote set-url origin /orderly/upstream
