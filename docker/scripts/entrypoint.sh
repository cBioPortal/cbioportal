source /root/.bashrc; \
/bin/bash $PORTAL_HOME/docker/scripts/addcBioAliases.sh; \
/bin/bash $PORTAL_HOME/docker/scripts/checkCustomConfig.sh; \
$CATALINA_HOME/bin/startup.sh; \
/bin/bash $PORTAL_HOME/docker/scripts/checkCustomFiles.sh; \
tail -f $CATALINA_HOME/logs/catalina.out