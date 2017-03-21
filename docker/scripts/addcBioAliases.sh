source /root/.bash_profile; \
echo 'alias cbio_start="source /root/.bash_profile; sh $CATALINA_HOME/bin/startup.sh && tail -f $CATALINA_HOME/logs/catalina.out"' >> /root/.bash_profile; \