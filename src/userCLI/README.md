## User CLI
For usage on first time deployment to bootstrap the app by adding some users with permissions
to the database.

### Usage

    image=vimc/orderly-web-user-cli:master
    docker run \
        -v orderly_volume:/orderly \
        $image <command>

or to test locally:
1. `./scripts/generate-test-data.sh` to create an orderly db in the top level directory
1. `./scripts/test-cli.sh <command>`

### Commands
#### Add users

    add-users test.user@example.com another.user@email.com

#### Add groups

    add-groups admin funder developer

#### Add members to group

    add-members admin test.user@example.com another.user@email.com

#### Grant permissions

    grant admin */reports.read */reports.review
    
Note that all users get their own identity group by default, so to grant a permission to a single user:
    
    grant test.user@example.com */reports.read
