#!/usr/bin/env bash

pip3 install -r scripts/release/requirements.txt
pip3 list
pytest scripts
