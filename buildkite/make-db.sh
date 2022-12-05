set -ex

here=$(dirname $0)
. $here/common

ORDERLY_IMAGE=$ORG/orderly:master
MIGRATE_IMAGE=$ORG/orderlyweb-migrate:$GIT_ID
ORDERLY_DEMO_SRC_PATH=$here/../src/app/orderly_demo

rm $ORDERLY_DEMO -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'run_orderly_demo' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    --env "HOME=/tmp" \
    $ORDERLY_IMAGE \
    $ORDERLY_DEMO_SRC_PATH \
    $ORDERLY_DEMO_PATH

docker run --rm -v $ORDERLY_DEMO:/orderly $MIGRATE_IMAGE
