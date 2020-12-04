# OrderlyWeb

[![Build Status](https://travis-ci.com/vimc/orderly-web.svg?branch=master)](https://travis-ci.com/vimc/orderly-web)
[![codecov.io](https://codecov.io/github/vimc/orderly-web/coverage.svg?branch=master)](https://codecov.io/github/vimc/orderly-web?branch=master)

See [spec.md](/docs/spec/spec.md) for the full API specification.

See [Release process](ReleaseProcess.md) for how to make a release 

## Developing
System requirements:
* **openjdk 8**: Install as per: https://openjdk.java.net/install/ Be sure to install the jdk package 
(for  development), not just the jre package.
* **Docker**
* **Docker Compose version 1.21.0:** This is version installed on the servers. There was a breaking change in 
v1.23.0 which appends random strings to container names each time they run.
* **Vault**
* **node 8**  https://nodejs.org/en/

1. Install Docker and add your user to the Docker group 
   (e.g. https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04.) 
   You may need to restart your machine for group changes to take effect.
1. Install node dependencies by running `npm install --prefix src/app/static`. Javascript and CSS will be compiled automatically as part of the gradle build.
1. For local development and testing run all dependencies (Orderly Server etc.) with `./dev/run-dependencies.sh`.
1. If running the app locally for manual testing, also add test user accounts with `./dev/add-test-users.sh`. You can then log in
with username "test.user@example.com" and password "password".
1. Run the app, either with `./gradlew :run` from the src dir, or through your IDE e.g by opening src/build.gradle as a 
   project in IntelliJ, which will display available gradle tasks in the UI. Follow the instructions for triggering a
   go signal. The app will now be available on your local machine at http://127.0.0.1:8888 and the API at http://127.0.0.1:8888/api/v1
1. If you want to manually test any functionality which requires interaction with the orderly server (e.g. publish or 
   run report), you will need the application to use the database in the `/git` subdirectory of `/src/app`, not the 
   default `/demo` subdirectory. This will give you access to the git test data, which only has the 'minimal' report, 
   not the full demo test data. 
   
   To achieve this, make the following changes before running `/dev/run-dependencies.sh` and the app:
   - In `/dev/cli.sh'` replace `docker run -v $PWD/src/app/demo:/orderly $image "$@"` with `docker run -v $PWD/src/app/git:/orderly $image "$@"`
   - In `/config/default.properties` replace `orderly_root=demo/` with `orderly_root=git/`
   - In `/dev/run-dependencies.sh` replace `export MONTAGU_ORDERLY_PATH=$(realpath $here/../src/customConfigTests/git` with `export MONTAGU_ORDERLY_PATH=$(realpath $here/../src/app/git)`    

See [auth.md](/docs/auth.md) for further details about web authentication.

### Generate test data.  
This is done automatically by `./dev/run-dependencies.sh` above. To generate a test
Orderly directory to develop against, run the `:app:generateTestData` gradle
task (either from within your IDE or on the command line from within the project
root directory with `./gradlew :app:generateTestData`).

The above task will generate two Orderly directories; one at `./src/app/demo`
and one at `./src/app/git`. The latter contains an Orderly directory which is
also a git repo, the former contains several different types of report. These
are used for integration tests and for running locally.

### Run tests
For all tests to pass you will need to run Montagu related dependencies with

        ./dev/run-dependencies.sh
        
Unit and integration tests are found in `src/app/src/test`. They can be run through the IDE or on the 
command line from the `src` directory with `./gradlew :app:test -i`

Selenium tests are found in `src/customConfigTests/src/test`. They can be run through the IDE or on the 
command line from the `src` directory with `./gradlew :customConfigTests:test -i`. You will have to run 
`./gradlew :customConfigTests:copyDemo` first.
You will also have to install chromedriver: `./scripts/install-chromedriver.sh` 

Javascript tests are in `src/app/static/src/tests` and can be run from the `static` directory
with `npm test`

Python tests of the release scripts are in `/scripts/release/tests` and can be run from the top level `orderly-web`
directory by running `./scripts/release/tests/test-release.sh`

### Regenerate database interface
```
cd src
# Make sure you have a fresh copy of the db
rm -r app/demo && rm rm -r app/git ./gradlew :app:generateTestData
# Generate the classes
./gradlew :generateDatabaseInterface
```

## Docker build
The app is dockerised by running `./buildkite/build-app.sh` which does the following:
1. Calls `./buildkite/make-build-env.sh` which builds a docker image based on the `Dockerfile` which contains all the gradle and npm dependencies needed to 
distribute the app. This image will also be re-used for the blackbox tests.
1. Builds the app specific build environment image based on `app.Dockerfile` which inherits from the above.
1. Generates an orderly-web database containing test data with `./buildkite/make-db.sh`
1. Runs all dependencies needed for tests as a docker network
1. Runs the image created in step 2. which tests the app and if successful, runs the `distDocker` task which builds and 
pushes the final docker image containing just the compiled app.

This script is designed to be run on Buildkite, but can also be run locally.
 
### Buildkite
The Buildkite build runs a series of independent steps, some of which are run in parallel. See `/buildkite/pipeline.yml` where
this is defined. These steps:
1. Build, test and push the database migrations image with `./buildkite/make-migrate-image.sh`.
1. Run `./buildkite/build-app.sh` which compiles code, run tests alongside a database containing test data and
 builds a Docker image containing the compiled app code (see steps [here](#docker-build))
1. Run `./buildkite/run-smoke-test.sh` which runs up the image and checks that the app starts ok
1. Run `./buildkite/run-custom-config-tests-in-container.sh`
1. Run `./buildkite/build-css-generator.sh` which creates a docker image that can compile the 
front-end sass to css - for usage during deployment to create custom style overrides 
(see [this deploy config](https://github.com/vimc/orderly-web-deploy/blob/d0691f6b84e590d09c50b15310a08fceabb7db98/config/customcss/orderly-web.yml))

For more explanation including a diagram that explains the relationship between the various docker images, 
see [build.md](/docs/build.md)

## Docker run
To make use of a built image, run:

        docker pull vimc/orderly-web:master
        docker run --rm -p 8888:8888 -v {PATH_TO_ORDERLY}:/orderly vimc/orderly-web:master

replacing `{PATH_TO_ORDERLY}` with an absolute path to an Orderly directory.

### User CLI
See [/src/userCLI/README.md](/src/userCLI/README.md)

## OrderlyWeb database schema

Tables required by OrderlyWeb, relating to presentation and access logic (e.g. users & permissions), are added to the 
Orderly database by this application. 

Code and migrations for 
this can be found in the ```migrations``` folder.  Migrations are run by the docker container defined by 
```migrations/Dockerfile``` which uses [Flyway](https://flywaydb.org/) to apply migrations defined in 
```migrations/sql``` using Flyway configiration defined in ```migrations/flyway.conf```.

```scripts/migrate-build.sh```, ```scripts/migrate-test.sh``` and ```scripts/migrate-push.sh``` 
are run as separate build steps in the TeamCity build configuration, to respectively
build the docker image, test it by running it and finally push it to the registry. 
Migrations can also be run on the local demo database with ```dev/migrate-local-test.sh``` 

We don't create a schema as such in the Orderly database, as Sqlite does not support schema. Instead we prefix all our 
tables' names with "orderlyweb".

These tables are:
### orderlyweb_user
The users of OrderlyWeb, however they are authenticated.

### orderlyweb_user_group
A user group could be something like 'report reviewers' or 'Ebola team'. Each individual user also gets their own group, 
because permissions are defined for user groups. 

### orderlyweb_user_group_user
Defines the membership for a user of a user group

### orderlyweb_permission
Defines a type of permission e.g. 'run reports'

### orderlyweb_report_tag
Defines tags at a report level

### orderlyweb_report_version
Holds report version data which is managed by OrderlyWeb rather than Orderly - currently just the Published flag.

### orderlyweb_report_version_tag
Defines tags at a report version level

### orderlyweb_user_group_permission
Links a user group to a permission to define that the user group has that permission. However that permission needs 
context to be fully specified, which may be either global level, report level or report version level, which will be 
found by joining to the following tables:

### orderlyweb_user_group_global_permission
Defines global level permissions. If a row in this table joins against a user_group_permission, then that user group has
that permission in all contexts

### orderlyweb_user_group_report_permission
Defines report level permissions. If one or more rows in this table joins against a user_group_permission then the 
group has that permission (e.g. read or run) in the context of the report(s) specified by the 'report' column values in 
the joining rows. 

### orderlyweb_user_group_version_permission
Defines report version level permissions. If one or more rows in this table joins against a user_group_permission then the 
group has that permission in the context of the report version(s) specified by the 'version' column values in the 
joining rows. 
