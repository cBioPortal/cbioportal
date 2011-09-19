#!/bin/bash -I

# Note, this script was intended to be run via cron.  Please ensure that the path to build.properties.GDAC
# points to an unchanging location and version of the file.  The path below is only meant for illustrative purposes.
$PORTAL_HOME/scripts/importUsers.py --properties-file $PORTAL_HOME/build.properties.GDAC 2>&1 > /dev/null
