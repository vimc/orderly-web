ORDERLY_IMAGE=$REGISTRY/orderly:master
MIGRATE_IMAGE=$REGISTRY/orderlyweb-migrate:$GIT_ID

rm demo -rf
rm git -rf

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    $ORDERLY_IMAGE \
    "."

docker run --rm -v ${PWD}/demo:/orderly $MIGRATE_IMAGE
docker run --rm -v ${PWD}/git:/orderly $MIGRATE_IMAGE
