#!/bin/bash

echo -ne "Checking if the variables are set...\t\t" &&
[ ! -z "${PORTAL_HOME} " ] && bash $PORTAL_HOME/core/src/main/scripts/env.sh && export ENV_CHECK=1 || export ENV_CHECK=0

if [ $ENV_CHECK -lt 1 ]
then
	echo "[ failed ]"
	exit -1
else
	echo "[ done ]"
fi

# Check if the portal.properties file is passed as an argument
if [ "$1" == "" ]
then
	echo "Usage: prepareClinicalTrialData.sh /path/to/portal.properties"
	exit -1
fi

SFTPUSER=`grep "nci.cancer.sftp.user" $1 |cut -f2 -d"="`
SFTPPASSWD=`grep "nci.cancer.sftp.password" $1 |cut -f2 -d"="`
SSHPASSPATH=`grep "sshpass.path" $1 |cut -f2 -d"="`

mkdir -p $PORTAL_DATA_HOME/reference-data
rm -rf $PORTAL_DATA_HOME/reference-data/clinical-trials

echo -ne "Downloading the PDQ XML files...\t\t" &&
cd $PORTAL_DATA_HOME/reference-data &&
$SSHPASSPATH -p "$SFTPPASSWD" sftp "$SFTPUSER@cancerinfo.nci.nih.gov:full/CTGovProtocol.tar.gz" > /dev/null &&
#wget --quiet --ftp-password=$FTPPASSWD --ftp-user=$FTPUSER ftp://cipsftp.nci.nih.gov/full/CTGovProtocol.tar.gz > /dev/null &&
echo "[ done ]" &&
echo -ne "Unzipping the archive...\t\t" &&
tar -zxf CTGovProtocol.tar.gz &&
mv CTGovProtocol clinical-trials &&
rm -f CTGovProtocol.tar.gz &&
echo "[ done ]" &&

# Go back to where we were
cd - > /dev/null
