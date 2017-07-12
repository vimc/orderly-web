# Run orderly to create demo data
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker run --rm --entrypoint create_orderly_demo.sh \
    -u ${UID} \
    -v ${PWD}:/orderly \
    -w /orderly \
    docker.montagu.dide.ic.ac.uk:5000/orderly:master \
    "./src/app/demo"
