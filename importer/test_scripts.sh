#!/bin/bash

pushd tests/ && PYTHONPATH=../scripts:$PYTHONPATH python -m unittest *.py; exit_stat=$?; popd; exit $exit_stat
