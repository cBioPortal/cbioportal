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
# WARNING: Be careful about publishing images generated like this publicly
# because your .git folder is exposed in the build step. We are not sure if
# this is a security risk: stackoverflow.com/questions/56278325
FROM maven:3.5.4 as build
COPY $PWD /cbioportal
WORKDIR /cbioportal
RUN mvn -DskipTests clean install

FROM openjdk:8-jre
RUN mkdir -p /cbioportal
COPY --from=build /cbioportal/portal/target/cbioportal*.war /app.war
COPY --from=build /cbioportal/portal/target/dependency/webapp-runner.jar /webapp-runner.jar
# copy over core jar and scripts
COPY --from=build /cbioportal/core /cbioportal/core
COPY --from=build /cbioportal/scripts /cbioportal/scripts
COPY --from=build /cbioportal/requirements.txt /cbioportal/requirements.txt

# install build and runtime dependencies
# ignore update failures
RUN apt-get update; apt-get install -y --no-install-recommends \
        build-essential \
        libmysql-java \
        default-libmysqlclient-dev \
        python3 \
        python3-setuptools \
        python3-dev \
        python3-pip \
	&& rm -rf /var/lib/apt/lists/* \
    && pip3 install wheel \
    && pip3 install -r /cbioportal/requirements.txt

# add importer scripts to PATH for easy running in containers
RUN find /cbioportal/core/src/main/scripts/ -type f -executable \! -name '*.pl'  -print0 | xargs -0 -- ln -st /usr/local/bin

# put config files in this folder if you want to override config
ENV PORTAL_HOME=/cbioportal
