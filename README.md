# Montagu Reporting API

## Developing
System requirements:
* openjdk 8
* Docker

Install Docker and add your user to the Docker group (e.g. https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04.) You may need to restart your machine for changes to take effect.

Configure your Docker client to use our registry by following instructions here:
https://github.com/vimc/montagu-ci#configuring-docker-clients-to-use-the-registry

To generate a test Orderly directory to develop against, run the `:generateTestData` gradle task 
(either from within your IDE or on the command line from within the project root directory with `./gradlew :generateTestData`)

The above task will generate an Orderly directory at `./src/app/demo`, which the app is configured to point to locally, and all tests to run against.

