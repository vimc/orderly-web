## User CLI
For usage on first time deployment to bootstrap the app by adding some users with permissions
to the database.

### Usage

    image=docker.montagu.dide.ic.ac.uk:5000/orderly-web-cli:master
    docker run \
        -v orderly_volume:/orderly \
        $image <command>

or to test locally:
1. `./scripts/generate-test-data.sh` to create an orderly db in the top level directory
1. `./scripts/test-cli.sh <command>`

### Commands
#### Add user

    add-user test.user@example.com

#### Add group

    add-group admin

#### Add members to group

    add-members admin test.user@example.com another.user@email.com

#### Grant permissions

    grant admin */reports.read */reports.review
    
Note that all users get their own identity group by default, so to grant a permission to a single user:
    
    grant test.user@example.com */reports.read
