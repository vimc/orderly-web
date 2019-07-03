#!/usr/bin/env bash
set -e

here=$(dirname $0)

$here/cli.sh add-users test.user@example.com
$here/cli.sh grant test.user@example.com */reports.read
$here/cli.sh grant test.user@example.com */reports.review
$here/cli.sh grant test.user@example.com */reports.run
