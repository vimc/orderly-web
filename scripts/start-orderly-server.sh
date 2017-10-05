# Run the orderly server
echo ${PWD}
docker run --rm -p 8123:8123 -v ${PWD}/demo:/orderly --user docker docker.montagu.dide.ic.ac.uk:5000/orderly.server:i648 /orderly

