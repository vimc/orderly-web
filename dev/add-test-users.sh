#!/usr/bin/env bash
set -e

here=$(dirname $0)

$here/cli.sh add-users test.user@example.com another.user@example.com a@some.com b@some.com c@some.com d@some.com
$here/cli.sh grant test.user@example.com */reports.read */reports.review */reports.run */users.manage */documents.read
$here/cli.sh add-groups Science Funders Admin
$here/cli.sh add-members Science test.user@example.com another.user@example.com
$here/cli.sh add-members Funders another.user@example.com a@some.com b@some.com c@some.com d@some.com
$here/cli.sh add-members Admin test.user@example.com
$here/cli.sh grant Funders */reports.read
$here/cli.sh grant Science report:minimal/reports.read

