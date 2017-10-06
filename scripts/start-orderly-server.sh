IMAGE=docker.montagu.dide.ic.ac.uk:5000/orderly.server:i648
docker pull $IMAGE
mkdir orderly
docker run --rm --entrypoint Rscript -v ${PWD}/orderly:/orderly --user $UID $IMAGE -e 'orderly:::prepare_orderly_git_example("/orderly")'

docker run --rm --entrypoint Rscript -v ${PWD}/orderly:/orderly --user $UID $IMAGE -e 'orderly::orderly_rebuild("/orderly")'

docker run --rm -p 8123:8123 -v ${PWD}/orderly:/orderly --user $UID $IMAGE /orderly
