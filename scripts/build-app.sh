set -e
git_id=$(git rev-parse --short HEAD)
git_branch=$(git symbolic-ref --short HEAD)

# Run orderly to create demo data
docker pull docker.montagu.dide.ic.ac.uk:5000/orderly:master

docker run --rm --entrypoint create_orderly_demo.sh \
    -u ${UID} \
    -v ${PWD}:./src/app/demo \
    -w ./src/app/demo \
    docker.montagu.dide.ic.ac.uk:5000/orderly:master \
    "./src/app/demo"

# Make the build environment image that is shared between multiple build targets
./scripts/make-build-env.sh

# Create an image based on the shared build env that compiles, tests and dockerises
# the app
docker build --tag montagu-reporting-api-app-build \
	--build-arg git_id=$git_id \
	--build-arg git_branch=$git_branch \
    -f app.Dockerfile \
	.

# Run the created image
docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --network=host \
    montagu-reporting-api-app-build
