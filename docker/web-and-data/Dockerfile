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
FROM maven:3-eclipse-temurin-21 as build

# download maven dependencies first to take advantage of docker caching
COPY pom.xml                                        /cbioportal/
COPY version.sh                                     /cbioportal/
WORKDIR /cbioportal

# RUN for subproject in */.; do cp version.sh "$subproject"; cd "$subproject"; mvn dependency:go-offline --fail-never; cd ..; done

RUN mvn dependency:go-offline --fail-never

COPY $PWD /cbioportal
RUN mvn install package -DskipTests -q
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*-exec.jar)

FROM eclipse-temurin:21

# download system dependencies first to take advantage of docker caching
RUN apt-get update; apt-get install -y --no-install-recommends \
        build-essential \
        default-mysql-client \
        default-libmysqlclient-dev \
        python3 \
        python3-setuptools \
        python3-dev \
        python3-pip \
        unzip \
    && rm -rf /var/lib/apt/lists/*

# copy over core files and data-related scripts
RUN mkdir -p /cbioportal

#Download core files
RUN wget https://github.com/cBioPortal/cbioportal-core/releases/download/v1.0.9/core-1.0.9.jar -P core/ ; cd core ; jar -xf core-1.0.9.jar scripts/ requirements.txt ; chmod -R a+x scripts/ ; cd ..;


COPY --from=build /cbioportal/src/main/resources/db-scripts /cbioportal/db-scripts

# install build and runtime dependencies
# ignore update failure980[1298[01 w2308s
RUN  pip3 install --break-system-packages -r /core/requirements.txt

# add importer scripts to PATH for easy running in containers
RUN find /core/scripts/ -type f -executable \! -name '*.pl' -print0 | xargs -0 -- ln -st /usr/local/bin

# put config files in this folder if you want to override config
ENV PORTAL_WEB_HOME=/cbioportal-webapp
ENV PORTAL_HOME=/cbioportal
ENV JAVA_OPTS="-Xms2G -Xmx8G"
ENV WEBAPP_OPTS=

# add exploded Spring Boot jar contents to image
# See: https://spring.io/guides/topicals/spring-boot-docker/
ARG DEPENDENCY=/cbioportal/target/dependency
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib                $PORTAL_WEB_HOME/lib/
COPY --from=build ${DEPENDENCY}/META-INF                    $PORTAL_WEB_HOME/META-INF/
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes             $PORTAL_WEB_HOME/

# add entrypoint
COPY --from=build /cbioportal/docker/web-and-data/docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["sh", "-c", "java $(echo $JAVA_OPTS) -cp /cbioportal-webapp:/cbioportal-webapp/lib/* org.cbioportal.PortalApplication $(echo $WEBAPP_OPTS)"]
