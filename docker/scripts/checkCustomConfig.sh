if [[ -e /custom_config/ ]]; then
	/bin/cp --force /custom_config/portal.properties /cbioportal/;
	/bin/cp --force /custom_config/log4j.properties /cbioportal/;
	. /root/.bashrc; /bin/cp --force /custom_config/context.xml $CATALINA_HOME/conf/context.xml;
fi