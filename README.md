# Montagu Reporting API

See [spec.md](/src/app/src/test/resources/spec/spec.md) for the full API specification.

## Developing
System requirements:
* openjdk 8
* Docker

1. Make sure you check out recursively, using `git clone --recursive URL`.
2. Install Docker and add your user to the Docker group 
   (e.g. https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04.) 
   You may need to restart your machine for changes to take effect.
3. Configure your Docker client to use our registry by following instructions 
   here: https://github.com/vimc/montagu-ci#configuring-docker-clients-to-use-the-registry

### Tasks
* Generate test data. To generate a test Orderly directory to develop against, 
  run the `:app:generateTestData` gradle task (either from within your IDE or on the
  command line from within the project root directory with 
  `./gradlew :app:generateTestData`)
  The above task will generate two Orderly directorys; one at `./src/app/demo` and one at `./src/app/git`. The latter contains an Orderly directory which is also a git repo, the former contains several different types of report. These are used for integration tests and for running locally.
* Run the Orderly server (responsible for running reports) using the `:app:startOrderlyServer` task.
* To run, use `:app:run`, having first set up a the test data using
  `:app:generateTestData` and started the orderly server using `:app:startOrderlyServer` 
* To run the tests, use `./gradlew test`, making sure that the app is not 
  currently running and you have already generated the test data and started the Orderly server.
  

## Docker build
The Teamcity build
1. Pulls in artifacts `demo` and `git` from the Orderly container build.
2. Runs `./scripts/make-build-env.sh` which builds a Docker image containing the source code and dependencies.
3. Runs `./scripts/build-app.sh` which uses the above image, mounting the `demo` folder as a volume, to compile code, run tests (for this purpose also running an Orderly Server image with the `git` folder mounted as a volume) and build a Docker image containing the compiled app code.

To build the image locally you will have to replace step one with generating the `demo` folder in your project directory following instructions here: https://github.com/vimc/orderly/tree/master/docker

## Docker run
To make use of a built image, run:

        docker pull docker.montagu.dide.ic.ac.uk:5000/montagu-reporting-api:master
        docker run --rm -p 8080:8080 -v {PATH_TO_ORDERLY}:/orderly docker.montagu.dide.ic.ac.uk:5000/montagu-reporting-api:master

replacing `{PATH_TO_OPRDERLY}` with an absolute path to an Orderly directory.

## Dependency on Montagu API
This API makes use of tokens from the Montagu API so can only be run in tandem with the latter with a shared public key for verifying tokens. For instructions on how to do this locally see steps 3 - 4 here: https://github.com/vimc/montagu-webapps/tree/master/app/src/main/report#development
