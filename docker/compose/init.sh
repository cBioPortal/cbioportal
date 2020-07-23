#!/usr/bin/env bash
for d in config data study; do
    cd $d; ./init.sh
    cd ..
done
