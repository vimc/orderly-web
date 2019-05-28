#!/usr/bin/env bash

commit_id=$(git rev-parse HEAD)

curl -X POST \
    -H "Accept: application/json" \
  --data-binary @src/app/coverage/test/jacocoTestReport.xml\
  "https://codecov.io/upload/v2?commit=${commit_id}&token=${CODECOV_TOKEN}"
