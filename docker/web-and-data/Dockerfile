# This image includes both the web app and the data import related scripts in
# one image. Because of historic reasons some of these scripts run java
# artifacts under the hood. It therefore currently makes more sense to have a
# single image supporting both instead of moving the scripts out to a separate
# container.
#
# Use from root directory of repo like:
#
# docker build -f docker/web-and-data/Dockerfile -t cbioportal-container:tag-name .
#
# NOTE: the .git folder is included in the build stage, but excluded 
# from the final image. No confidential information is exposed.
# (see: stackoverflow.com/questions/56278325)
FROM maven:3.5.4 as build

# download maven dependencies first to take advantage of docker caching
COPY pom.xml                                     /cbioportal/
COPY version.sh                                  /cbioportal/
WORKDIR /cbioportal

COPY business/pom.xml                         business/
COPY core/pom.xml                             core/
COPY db-scripts/pom.xml                       db-scripts/
COPY model/pom.xml                            model/
COPY persistence/pom.xml                      persistence/
COPY persistence/persistence-api/pom.xml      persistence/persistence-api/
COPY persistence/persistence-mybatis/pom.xml  persistence/persistence-mybatis/
COPY scripts/pom.xml                          scripts/
COPY security/pom.xml                         security/
COPY security/security-spring/pom.xml         security/security-spring/
COPY service/pom.xml                          service/
COPY web/pom.xml                              web/
RUN for subproject in */.; do cp version.sh "$subproject"; cd "$subproject"; mvn dependency:go-offline --fail-never; cd ..; done
COPY portal/pom.xml                           portal/
RUN mvn dependency:go-offline --fail-never

COPY $PWD /cbioportal
RUN mvn -DskipTests clean install

FROM openjdk:8-jre

# download system dependencies first to take advantage of docker caching
RUN apt-get update; apt-get install -y --no-install-recommends \
        build-essential \
        default-mysql-client \
        default-libmysqlclient-dev \
        python3 \
        python3-setuptools \
        libpython-dev \
        python3-dev \
        python3-pip \
	&& rm -rf /var/lib/apt/lists/* \
    && pip3 install wheel

ENV PORTAL_WEB_HOME=/cbioportal-webapp

RUN mkdir -p /cbioportal
COPY --from=build /cbioportal/portal/target/cbioportal*.war /app.war
COPY --from=build /cbioportal/portal/target/dependency/webapp-runner.jar /webapp-runner.jar
# copy over core jar and scripts
COPY --from=build /cbioportal/core /cbioportal/core
COPY --from=build /cbioportal/scripts /cbioportal/scripts
COPY --from=build /cbioportal/db-scripts /cbioportal/db-scripts
COPY --from=build /cbioportal/requirements.txt /cbioportal/requirements.txt

# install build and runtime dependencies
# ignore update failures
RUN pip3 install awscli
RUN pip3 install -r /cbioportal/requirements.txt

# add importer scripts to PATH for easy running in containers
RUN find /cbioportal/core/src/main/scripts/ -type f -executable \! -name '*.pl'  -print0 | xargs -0 -- ln -st /usr/local/bin

# put config files in this folder if you want to override config
ENV PORTAL_HOME=/cbioportal

RUN mkdir -p $PORTAL_WEB_HOME
RUN unzip /app.war -d  $PORTAL_WEB_HOME
