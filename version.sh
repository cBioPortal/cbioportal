#!/usr/bin/env bash
if [ -n "$PROJECT_VERSION" ]; then
    # use env variable PROJECT_VERSION if defined
    echo $PROJECT_VERSION
elif ! [ -x "$(command -v git)" ]; then
    # git is not installed, 
    echo '0-unknown-version'
else
	# get human readable commit id from git
	git describe --tags --always --dirty | sed 's/^v//'
fi
