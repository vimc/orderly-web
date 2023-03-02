# OrderlyWeb

[![Build status](https://badge.buildkite.com/54990e80cd2495e45bbe78d46afaee17147451bd4d620aacbf.svg?branch=master)](https://buildkite.com/mrc-ide/orderly-web)
[![codecov.io](https://codecov.io/github/vimc/orderly-web/coverage.svg?branch=master)](https://codecov.io/github/vimc/orderly-web?branch=master)

See [spec.md](/docs/spec/spec.md) for the full API specification.

See [Release process](ReleaseProcess.md) for how to make a release 

## Developing
System requirements:
* **openjdk 11**: Install as per: https://openjdk.java.net/install/ Be sure to install the jdk package 
(for  development), not just the jre package.
* **Docker**
* **Docker Compose**: Install as per: https://docs.docker.com/compose/install/
* **Vault**: Install as per: https://learn.hashicorp.com/tutorials/vault/getting-started-install
* **node 19**:  https://nodejs.org/en/ (ships with npm@8.19.2)

1. Install Docker and add your user to the Docker group 
   (e.g. https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04.) 
   You may need to restart your machine for group changes to take effect.
1. Install node dependencies by running `npm install --prefix src/app/static`. Javascript and CSS will be compiled automatically as part of the gradle build.
1. For local development and testing run all dependencies (Orderly Server etc.) with `./dev/run-dependencies.sh`.
1. If running the app locally for manual testing, also add test user accounts with `./dev/add-test-users.sh`. You can then log in
with username "test.user@example.com" and password "password".
1. Run the app, either with: 
    - `./gradlew :run` from the src dir; 
    - or through your IDE e.g by opening src/build.gradle as a project in IntelliJ, which will display available gradle tasks in the UI. 
1. Follow the instructions in the CLI logs for triggering a go signal. 
Linux users may need to create the directory from their root directory first `sudo mkdir -p /etc/orderly/web` and then add the go_signal file `sudo touch /etc/orderly/web/go_signal`
The app will now be available on your local machine at http://127.0.0.1:8888 and the API at http://127.0.0.1:8888/api/v1
   
See [auth.md](/docs/auth.md) for further details about web authentication.

### Generate test data.  
This is done automatically by `./dev/run-dependencies.sh` above. To generate a test
Orderly directory to develop against, run the `:app:generateTestData` gradle
task (either from within your IDE or on the command line from within the project
root directory with `./gradlew :app:generateTestData`).

The above task will generate an Orderly directory at `./src/app/demo` which is also a git repo. This
is used for integration tests and for running locally.

### Run tests
For all tests to pass you will need to run Montagu related dependencies with

        ./dev/run-dependencies.sh
        
Unit and integration tests are found in `src/app/src/test`. They can be run through the IDE or on the 
command line from the `src` directory with `./gradlew :app:test -i`. The orderly state is shared between 
tests, so tests should generally avoid mutating state. Tests that involve running reports can run the 
dedicated report: "minimal-for-running"; that way, all other reports will have a determinate number of versions.

Selenium tests are found in `src/customConfigTests/src/test`. They can be run through the IDE or on the 
command line from the `src` directory with `./gradlew :customConfigTests:test`. 
You will have to install chromedriver: `./scripts/install-chromedriver.sh`.
sl4j logging is disabled by default to make the output more legible; if needed for debugging, the log level
can be configured by modifying `src/customConfigTests/src/test/resources/simplelogger.properties`. Also by default, 
only `stderr` is printed to the console while running these tests; to get `stdout` as well, run in info/verbose mode 
with the `-i` flag: `./gradlew :customConfigTests:test -i`

Javascript tests are in `src/app/static/src/tests` and can be run from the `static` directory
with `npm test`

Python tests of the release scripts are in `/scripts/release/tests` and can be run from the top level `orderly-web`
directory by running `./scripts/release/tests/test-release.sh`

### Code linting

All new code should follow our [code style conventions]. We have [settings files] that configure IDEA to format code
appropriately.

To run the linter ([detekt](https://detekt.github.io/detekt/index.html)) against the non-test code:

```sh
cd src
./gradlew :app:detektMain
```

- Issues that pre-date the introduction of linting to the codebase are listed in `src/config/detekt/baseline-main.yml`
- This baseline should not be edited by hand: if you are editing existing code that includes exemptions then you can
  choose either to resolve the relevant issues (e.g. if there are a very limited number) or regenerate the baseline
  via `./gradlew :app:detektBaselineMain` *after ensuring that any new code does conform to the conventions*
- Any unavoidable exemptions for new code should be made via `@Suppress` annotations (with a justification in the PR
  message) rather by further additions to the baseline

Note that the linter is currently unable to detect some cases where code doesn't follow the style conventions. In these
cases the conventions take precedence. In particular: **braces should always be explicit, and placed on new line**.

The `detektMain` task is preferred to `detekt` as it uses
[type resolution](https://detekt.github.io/detekt/type-resolution.html) to perform more advanced code analysis.
`detektMain` uses the `baseline-main.yml` file. This is symlinked to `baseline.yml` so that the `detekt` task also
works, which is necessary because it is called implicitly during the Docker image build stage.

[code style conventions]: https://mrc-ide.myjetbrains.com/youtrack/articles/RESIDE-A-8/Code-style-conventions

[settings files]: https://github.com/vimc/orderly-web/tree/master/src/.idea/codeStyles

Front-end linting is provided by `eslint`, settings [here](https://github.com/vimc/orderly-web/tree/master/src/app/static/.eslintrc.js).
To lint the front-end code run `npm run lint`, or to run and automatically fix issues `npm run lint -- --fix`

### Coverage

The `@NoCoverage` annotation defined in [Annotations.kt](src/app/src/main/kotlin/org/vaccineimpact/orderlyweb/Annotations.kt)
excludes specified classes from processing by JaCoCo in order to avoid the necessity of writing artificial test logic
simply to satisfy coverage metrics. It is only intended for use with `internal data class`es used to
(de)serialise orderly.server responses.

### Regenerate database interface
```
cd src
# Make sure you have a fresh copy of the db
./gradlew :app:generateTestData
# Generate the classes
./gradlew :generateDatabaseInterface
```

### Automatic rebuilding of UI code

Running `npm run watch --prefix src/app/static` starts a process that will rebuild the Javascript whenever a `.js`, `.ts` or
`.vue` file changes. If you're just making front-end changes this avoids having to restart the whole application
(recompiling the Kotlin code etc).

## Docker build
The app is dockerised by running the `./buildkite/build-app.sh` script, which does the following:
1. Calls `./buildkite/make-build-env.sh` which builds a docker image based on the `Dockerfile` which contains all the
gradle and npm dependencies needed to distribute the app. This image will also be re-used for the blackbox tests.
1. Builds the app specific build environment image based on `app.Dockerfile` which inherits from the above.
1. Generates an orderly-web database containing test data with `./buildkite/make-db.sh`
1. Runs all dependencies needed for tests as a docker network
1. Runs the image created in step 2. which tests the app and if successful runs the `distDocker` task which builds the
   final docker image containing just the compiled app, and then tags the image and uploads it to Docker Hub
1. Archives the linting and test reports, and uploads test coverage results to Codecov

This script is designed to be run on Buildkite but can also be run locally, in which case you will need to set the
following environment variable for Codecov:
```bash
export CODECOV_TOKEN=xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx # See https://app.codecov.io/gh/vimc/orderly-web/settings
```
 
### Buildkite
The Buildkite build runs a series of independent steps, some of which are run in parallel. See `/buildkite/pipeline.yml` where
this is defined. These steps:
1. Build, test and push the database migrations image with `./buildkite/make-migrate-image.sh`.
1. Run `./buildkite/build-app.sh` which compiles the code, run tests alongside a database containing test data, builds a
   Docker image containing the compiled app code (see steps [here](#docker-build)) and uploads test results as artifacts
1. Run `./buildkite/run-smoke-test.sh` which runs up the image and checks that the app starts ok
1. Run `./buildkite/run-custom-config-tests-in-container.sh`
1. Run `./buildkite/build-css-generator.sh` which creates a docker image that can compile the 
front-end sass to css - for usage during deployment to create custom style overrides 
(see [this deploy config](https://github.com/vimc/orderly-web-deploy/blob/d0691f6b84e590d09c50b15310a08fceabb7db98/config/customcss/orderly-web.yml))
1. Run `./buildkite/build-standalone-image.sh` which builds and pushes a "standalone" docker image for running a local OW instance in noAuth mode with a single docker container.
For more explanation including a diagram that explains the relationship between the various docker images, 
see [build.md](/docs/build.md)

## Docker run
To make use of a built image, run:

        docker pull vimc/orderly-web:master
        docker run --rm -p 8888:8888 -v {PATH_TO_ORDERLY}:/orderly vimc/orderly-web:master

replacing `{PATH_TO_ORDERLY}` with an absolute path to an Orderly directory.

The resulting app will only work if it has an `orderly.server` instance it can talk to, and if database migrations have been run.
To run a standalone image that works without `orderly.server` (so no report running) and handles database migrations itself, run
	
	docker pull vimc/orderly-web-standalone:master
	docker run --rm -p 8888:8888 -v {PATH_TO_ORDERLY}:/orderly vimc/orderly-web-standalone:master

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

### orderlyweb_report_run
Information about a report run i.e. a job sent to orderly.server that is expected to result in a new report version.
Includes metadata about the relevant report (name, git commit etc), runtime parameters, and outputs (logs etc).
Note that the `key` column is only valid while the job is in progress. The report_id column is populated only on
successful job completion.

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

### orderlyweb_workflow_run
Information about a workflow run i.e. a job sent to orderly.server that is expected to result in one or more new report
versions. Includes a list of parameterised reports and metadata about the workflow itself (name, owner, invocation
timestamp etc). Workflows are unique by any of name+timestamp (presented in UI), key (orderly.server's reference) or ID
(OrderlyWeb's primary identifier).

### orderlyweb_workflow_run_reports
Stores information relating to workflows and reports. This table references key column of orderlyweb_workflow_run. 
