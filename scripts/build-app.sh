set -e
git_id=$(git rev-parse --short HEAD)
git_branch=$(git symbolic-ref --short HEAD)

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag montagu-reporting-api-app-build \
	--build-arg git_id=$git_id \
	--build-arg git_branch=$git_branch \
    -f app.Dockerfile \
	.

docker pull docker.montagu.dide.ic.ac.uk:5000/orderly.server:master

echo $UID

docker run --rm \
    -p 8123:8123 \
    -v $PWD/git:/orderly \
    --user $UID \
    --network=host \
    docker.montagu.dide.ic.ac.uk:5000/orderly.server:master "orderly" &

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $PWD/demo:/api/src/app/demo \
    -v $PWD/git:/api/src/app/git \
    --network=host \
    montagu-reporting-api-app-build
