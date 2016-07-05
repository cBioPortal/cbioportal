FROM tomcat:8-jre8
#============== Install Required Components ================#
RUN apt-get update && apt-get install -y --no-install-recommends \
                git \
                libmysql-java \
                maven \
                openjdk-8-jdk="$JAVA_DEBIAN_VERSION" \
                python \
                python-jinja2 \
                python-mysqldb \
                python-requests \
    && rm -rf /var/lib/apt/lists/*
# set up Tomcat to use the MySQL Connector/J Java connector
RUN ln -s /usr/share/java/mysql-connector-java.jar "$CATALINA_HOME"/lib/; \
rm -rf $CATALINA_HOME/webapps/examples
#== Set Default Config & Build from Source ==#
COPY . /cbioportal
RUN	echo "export PORTAL_HOME=/cbioportal" >> /root/.bashrc; . /root/.bashrc; \
	cp $PORTAL_HOME/src/main/resources/portal.properties.EXAMPLE $PORTAL_HOME/src/main/resources/portal.properties; \
	cp $PORTAL_HOME/src/main/resources/log4j.properties.EXAMPLE $PORTAL_HOME/src/main/resources/log4j.properties; \
	/bin/cp -u --force $PORTAL_HOME/docker/build/context.xml $CATALINA_HOME/conf/context.xml; \
	/bin/cp -u --force $PORTAL_HOME/docker/build/gene_sets.txt $PORTAL_HOME/core/src/main/resources/; \
	mkdir /root/.m2/; cp /cbioportal/docker/build/settings.xml /root/.m2/; \
	cd $PORTAL_HOME; mvn -DskipTests clean install
#======== Copy cBioPortal .war file ========#
RUN . /root/.bashrc; cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/
#======== cBioPortal Startup ===============#
CMD . /root/.bashrc; \
    cp /cbio_config/portal.properties $PORTAL_HOME/src/main/resources/portal.properties; \
    cp /cbio_config/log4j.properties.EXAMPLE $PORTAL_HOME/src/main/resources/log4j.properties; \
    /bin/cp -u --force cp /cbio_config/context.xml $CATALINA_HOME/conf/context.xml; \
    /bin/cp -u --force $PORTAL_HOME/docker/build/gene_sets.txt $PORTAL_HOME/core/src/main/resources/; \
    mkdir /root/.m2/; cp /cbio_config/settings.xml /root/.m2/; \
    cd $PORTAL_HOME; mvn -DskipTests clean install/bin/bash; \
    cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/; \
    cd $CATALINA_HOME; \
    ./bin/startup.sh; \
    tail -f ../logs/catalina.out
#===========================================#