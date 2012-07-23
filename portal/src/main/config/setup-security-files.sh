#!/bin/bash

#
# called by maven to setup the proper web.xml and applicationContext-security.xml files.
#
# a single argument (boolean) is passed to indicate if security is enabled
#
security_enabled=$1
if [ $security_enabled = "true" ]
then
	echo 'security is enabled'
	cp src/main/resources/security_config/applicationContext-security.xml src/main/resources/applicationContext-security.xml
	cp src/main/resources/security_config/web-security.xml src/main/webapp/WEB-INF/web.xml
else
	echo 'security is disabled'
	cp src/main/resources/security_config/applicationContext-security-disabled.xml src/main/resources/applicationContext-security.xml
	cp src/main/resources/security_config/web.xml src/main/webapp/WEB-INF/web.xml
fi