. /root/.bashrc; \
echo 'alias cbio_start=". /root/.bashrc; sh $CATALINA_HOME/bin/startup.sh && tail -f $CATALINA_HOME/logs/catalina.out"' >> /root/.bashrc; \