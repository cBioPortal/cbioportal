FROM tomcat:8-jre8
MAINTAINER Alexandros Sigaras <als2076@med.cornell.edu>, Fedde Schaeffer <fedde@thehyve.nl>
LABEL Description="cBioPortal for Cancer Genomics"
ENV APP_NAME="cbioportal" \
    PORTAL_HOME="/cbioportal"
#======== Install Prerequisites ===============#
RUN apt-get update && apt-get install -y --no-install-recommends \
        git \
        libmysql-java \
        patch \
        python3 \
        python3-jinja2 \
        python3-mysqldb \
        python3-requests \
        maven \
        openjdk-8-jdk \
    && ln -s /usr/share/java/mysql-connector-java.jar "$CATALINA_HOME"/lib/ \
    && rm -rf $CATALINA_HOME/webapps/examples \
    && rm -rf /var/lib/apt/lists/*
#======== Configure cBioPortal ===========================#
COPY . $PORTAL_HOME
WORKDIR $PORTAL_HOME
EXPOSE 8080
#======== Build cBioPortal on Startup ===============#
CMD mvn -DskipTests clean install \
     && cp $PORTAL_HOME/portal/target/cbioportal*.war $CATALINA_HOME/webapps/cbioportal.war \
     && find $PWD/core/src/main/scripts/ -type f -executable \! -name '*.pl'  -print0 | xargs -0 -- ln -st /usr/local/bin \
     && sh $CATALINA_HOME/bin/catalina.sh run
