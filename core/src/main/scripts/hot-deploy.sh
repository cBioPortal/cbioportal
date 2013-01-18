#!/bin/bash

# convenience script to drive $PORTAL_HOME/portal/scripts/hotDeploy.py
# example usage:
#
# ./hot-deploy.sh miso.cbio.mskcc.org grossb v1.1.5

# setup the enviroment vars (maybe easier to source bashrc, etc, like . /home/user/.bashrc)
PYTHONHOME=
PYTHONPATH=
PORTAL_HOME=

# host, user, rev (mercurial) get passed in command line
HOST=$1
USER=$2
REV=$3

# Note, this script was intended to be run via cron.  Please ensure that the path to portal.properties.*
$PORTAL_HOME/portal/scripts/hotDeploy.py --credentials $PORTAL_HOME/src/main/resources/properties.txt --portal-properties $PORTAL_HOME/src/main/resources/portal.properties.GDAC --rev $REV --host $HOST --user $USER
