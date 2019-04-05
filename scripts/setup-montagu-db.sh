MIGRATE_IMAGE=docker.montagu.dide.ic.ac.uk:5000/montagu-migrate:${MONTAGU_DB_VERSION}

docker pull ${MIGRATE_IMAGE}
docker run --rm --network=$NETWORK \
                     ${MIGRATE_IMAGE} \
                     migrate

here=$(dirname $0)
$here/montagu-cli.sh add "Test User" test.user \
    test.user@example.com password \

$here/montagu-cli.sh addRole test.user user
$here/montagu-cli.sh addRole test.user admin
