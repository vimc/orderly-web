ORDERLY_IMAGE=docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker pull $ORDERLY_IMAGE
docker run --rm \
    --entrypoint 'create_orderly_demo.sh' \
    -u $UID \
    -v $PWD:/orderly \
    -w "/orderly" \
    $ORDERLY_IMAGE \
    "."

MIGRATE_IMAGE=docker.montagu.dide.ic.ac.uk:5000/orderlyweb-migrate:master

docker pull $MIGRATE_IMAGE

rm demo -rf
rm git -rf

docker run --rm \
    -v $PWD/demo:/orderly \
    $MIGRATE_IMAGE

docker run --rm \
    -v $PWD/git:/orderly \
    $MIGRATE_IMAGE
