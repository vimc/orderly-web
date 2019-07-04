#!/usr/bin/env bash

pip3 install -r scripts/release/requirements.txt
python3 -m pytest scripts
