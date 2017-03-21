source /root/.bash_profile; \
/bin/bash /cbio_scripts/addcBioAliases.sh; \
/bin/bash /cbio_scripts/checkCustomConfig.sh; \
$CATALINA_HOME/bin/startup.sh; \
/bin/bash /cbio_scripts/checkCustomFiles.sh; \
tail -f $CATALINA_HOME/logs/catalina.out