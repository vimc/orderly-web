# OrderlyWeb

See [spec.md](/src/app/src/test/resources/spec/spec.md) for the full API specification.

## Developing
System requirements:
* **openjdk 8**: Install as per: https://openjdk.java.net/install/ Be sure to install the jdk package 
(for  development), not just the jre package.
* **Docker**
* **Docker Compose version 1.21.0:** This is version installed on the servers. There was a breaking change in 
v1.23.0 which appends random strings to container names each time they run.
* **Vault**

1. Install Docker and add your user to the Docker group 
   (e.g. https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04.) 
   You may need to restart your machine for group changes to take effect.
1. Configure your Docker client to use our registry by following instructions 
   here: https://github.com/vimc/montagu-registry/tree/master#login 
   
   This is where you will need to have Vault 
   installed, and the VAULT_ADDR variable specified, e.g. by adding 
   `export VAULT_ADDR='https://support.montagu.dide.ic.ac.uk:8200'` to your profile using ` sudo nano ~/.profile` 
1. Run all dependencies (Orderly Server etc.) with 
   `./dev/run-dependencies.sh`
1. Run the app, either with `./gradlew :run` from the src dir, or through your IDE e.g by opening src/build.gradle as a 
   project in IntelliJ, which will display available gradle tasks in the UI. Follow the instructions for triggering a
   go signal. The app will now be available on your local machine at http://127.0.0.1:8081 

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
To run the tests, 
1. run `./dev/run-dependencies.sh` from this directory
2. run `./gradlew test` from the src dir

## Authenticating with the local app
### Montagu
This is an extremely basic first pass.
1. run `./dev/run-dependencies.sh`
1. run a copy of [montagu-webapps](https://github.com/vimc/montagu-webapps) admin portal 
locally with `npm run admin`
1. log in to the locally running admin portal
1. now visiting any OrderlyWeb page should automatically log you in

## Regenerate database interface
```
cd src
# Make sure you have a fresh copy of the db
rm -r app/demo && rm rm -r app/git ./gradlew :app:generateTestData
# Generate the classes
./gradlew :generateDatabaseInterface
```

## Docker build
The Teamcity build
1. Pulls in artifacts `demo` and `git` from the Orderly container build.
2. Runs `./scripts/make-build-env.sh` which builds a Docker image containing the source code and dependencies.
3. Runs `./scripts/build-app.sh` which uses the above image, mounting the `demo` folder as a volume, to compile code, 
run tests (for this purpose also running an Orderly Server image with the `git` folder mounted as a volume) and build a
 Docker image containing the compiled app code.

To build the image locally you will have to replace step one with generating the `demo` folder in your project 
directory following instructions here: https://github.com/vimc/orderly/tree/master/docker

## Docker run
To make use of a built image, run:

        docker pull docker.montagu.dide.ic.ac.uk:5000/orderly-web:master
        docker run --rm -p 8080:8080 -v {PATH_TO_ORDERLY}:/orderly docker.montagu.dide.ic.ac.uk:5000/orderly-web:master

replacing `{PATH_TO_ORDERLY}` with an absolute path to an Orderly directory.


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
Migrations can also be tested locally with ```scripts/migrate-local-test.sh``` 

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

