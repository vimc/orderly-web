#!/usr/bin/env bash
set -ex

if [ ! -f "app/demo/orderly.sqlite" ]
then
	./gradlew :app:generateTestData
fi

# delete directory if it already exists
if [ -d "/etc/montagu/reports_api/token_key" ] 
then
   rm /etc/montagu/reports_api/token_key -r
fi

# copy key from api directory
mkdir -p /etc/montagu/reports_api/token_key
cp -R /etc/montagu/api/token_key/public_key.der /etc/montagu/reports_api/token_key/

ls /etc/montagu/reports_api/token_key
./gradlew :run
