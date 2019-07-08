#!/usr/bin/env bash

pip3 install -r scripts/release/requirements.txt --quiet
python3 -m pytest scripts
