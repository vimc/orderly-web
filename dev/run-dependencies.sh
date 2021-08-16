#!/usr/bin/env bash
set -e

here=$(dirname $0)
(
	cd $here/../src
	# get fresh tests data
	rm -rf app/demo
	rm -rf app/git
	./gradlew :generateTestData
	./gradlew :customConfigTests:copyGitDemo
)

$here/migrate-local-test.sh

git --git-dir=$here/../src/app/git/.git remote set-url origin /orderly/upstream 

export MONTAGU_ORDERLY_PATH=$(realpath $here/../src/app/git)

export ORDERLY_SERVER_USER_ID=$UID
$here/../scripts/run-dependencies.sh

echo "Dependencies are now running; press Ctrl+C to teardown"

# From now on, if the user presses Ctrl+C we should teardown
function cleanup() {
    docker-compose -f $here/../scripts/docker-compose.yml --project-name montagu down
}
trap cleanup INT

sleep infinity
