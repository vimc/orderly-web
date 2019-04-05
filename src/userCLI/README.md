## User CLI
For usage on first time deployment to bootstrap the app by adding some users with permissions
to the database.

### Add user

    image=docker.montagu.dide.ic.ac.uk:5000/orderly-web-cli:master
    docker run \
        -v orderly_volume:/orderly \
        $image add-user test.user@example.com
