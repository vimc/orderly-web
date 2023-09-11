#!/usr/bin/env bash

apt-get update
apt-get install -y unzip xvfb libxi6 libgconf-2-4

# See https://chromedriver.chromium.org/downloads/version-selection
CHROME_DRIVER_URL=https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/$(google-chrome --product-version)/linux64/chromedriver-linux64.zip
echo Fetching from $CHROME_DRIVER_URL
curl -O $CHROME_DRIVER_URL
unzip chromedriver-linux64.zip
mv chromedriver /usr/bin/chromedriver
chown root:root /usr/bin/chromedriver
chmod +x /usr/bin/chromedriver
