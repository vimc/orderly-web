# Building the images

The build process is fairly complicated. There is a shared build environment image based on the `Dockerfile` in 
this repository and built by `./buildkite/make-build-env.sh` that has node, docker and openjdk installed.
 3 further images inherit from this one: 
1. a build environment for compiling the main app
1. a build environment for compiling the user cli
1. an image that also contains chromedriver and runs the custom config tests
 
The dockerfiles that the main app and user cli are based on are generated by the gradle `distDocker` plugin.
 
The relationship between the various docker images may be best understood by way of a diagram:

![OrderlyWeb build process](/docs/orderlywebbuild.png?raw=true) 

The only images in this diagram that get pushed to the docker registry are `orderly-web` and `orderly-web-user-cli`.
The migrations image, `orderlyweb-migrate`, is also pushed to the registry but is not included in this diagram as it is completely independent of the other images and 
`./scripts/migrate-build.sh` should be self-explanatory.
