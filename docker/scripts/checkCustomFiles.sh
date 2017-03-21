if [[ -e /custom_files/ ]]; then
	source /root/.bash_profile; \
	sleep 5; \
	/bin/cp --force /custom_files/cbiologo.png $CATALINA_HOME/webapps/cbioportal/images;
	/bin/cp --force /custom_files/header_bar.jsp $CATALINA_HOME/webapps/cbioportal/WEB-INF/jsp/global/header_bar.jsp
fi