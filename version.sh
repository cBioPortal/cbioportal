#!/usr/bin/env bash
if [ -n "$PROJECT_VERSION" ]; then
    # use env variable PROJECT_VERSION if defined
    echo $PROJECT_VERSION
elif ! [ -x "$(command -v git)" ] || ! [ -d .git ]; then
	# git is not installed, or not in a git repo
    echo '0-unknown-version'
else
	# get human readable commit id from git
	git describe --tags --always --dirty | sed 's/^v//'
fi
