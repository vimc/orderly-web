#!/usr/bin/env bash

apt-get update
apt-get install -y unzip xvfb libxi6 libgconf-2-4 jq

# See https://chromedriver.chromium.org/downloads/version-selection
CHROME_DRIVER_URL=https://storage.googleapis.com/chrome-for-testing-public/$(curl "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions.json" | jq --raw-output '.channels.Stable.version')/linux64/chromedriver-linux64.zip

echo Fetching from $CHROME_DRIVER_URL
curl -O $CHROME_DRIVER_URL
unzip chromedriver-linux64.zip
mv chromedriver-linux64/chromedriver /usr/bin/chromedriver
chown root:root /usr/bin/chromedriver
chmod +x /usr/bin/chromedriver
