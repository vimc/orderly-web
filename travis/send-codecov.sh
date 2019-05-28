#!/usr/bin/env bash

commit_id=$(git rev-parse HEAD)

touch codecovreports
cat ./src/app/coverage/test/*.xml >> codecovreports
echo '<<<<<< EOF' >> codecovreports

curl -X POST \
    -H "Accept: application/json" \
  --data-binary @codecovreports \
  "https://codecov.io/upload/v2?commit=${commit_id}&token=${CODECOV_TOKEN}"
