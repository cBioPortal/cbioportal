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
            && rm -rf /var/lib/apt/lists/* \
            && ln -s /usr/share/java/mysql-connector-java.jar "$CATALINA_HOME"/lib/ \
            && rm -rf $CATALINA_HOME/webapps/examples \
            && mkdir /root/.m2/
ENV PORTAL_HOME /cbioportal
COPY . $PORTAL_HOME
WORKDIR $PORTAL_HOME
EXPOSE 8080
#======== Build cBioPortal on Startup ===============#
CMD cp /cbio_config/portal.properties $PORTAL_HOME/src/main/resources/portal.properties \
    && cp /cbio_config/log4j.properties $PORTAL_HOME/src/main/resources/log4j.properties \
    && cp /cbio_config/log4j.properties $PORTAL_HOME/log4j.properties \
    && cp /cbio_config/context.xml $CATALINA_HOME/conf/context.xml \
    && cp /cbio_config/gene_sets.txt $PORTAL_HOME/core/src/main/resources/ \
    && cp /cbio_config/settings.xml /root/.m2/ \
    && mvn -DskipTests clean install \
    && cp $PORTAL_HOME/portal/target/cbioportal.war $CATALINA_HOME/webapps/ \
    && sh $CATALINA_HOME/bin/catalina.sh run