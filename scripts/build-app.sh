set -e
git_id=$(git rev-parse --short HEAD)
git_branch=$(git symbolic-ref --short HEAD)
export ORDERLY_SERVER_VERSION=$(<src/config/orderly_server_version)

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag montagu-reporting-api-app-build \
	--build-arg git_id=$git_id \
	--build-arg git_branch=$git_branch \
    -f app.Dockerfile \
	.

# Run the orderly server
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION

docker run --rm \
    -p 8123:8123 \
    -d \
    -v $PWD/git:/orderly \
    --network=host \
    docker.montagu.dide.ic.ac.uk:5000/orderly.server:$ORDERLY_SERVER_VERSION "orderly"

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    montagu-reporting-api-app-build
