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
ARG CORE_BRANCH=rfc100-rc

FROM maven:3-eclipse-temurin-21 as build

# download maven dependencies first to take advantage of docker caching
COPY pom.xml                                        /cbioportal/
COPY version.sh                                     /cbioportal/
WORKDIR /cbioportal

# RUN for subproject in */.; do cp version.sh "$subproject"; cd "$subproject"; mvn dependency:go-offline --fail-never; cd ..; done

RUN mvn dependency:go-offline --fail-never

COPY $PWD /cbioportal
RUN touch /cbioportal/src/main/resources/application.properties
RUN mvn install package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*-exec.jar)

FROM maven:3-eclipse-temurin-21 as build-core
ARG CORE_BRANCH
# This invalidates the Docker cache iff the latest commit hash for the CORE_BRANCH has changed
ADD https://api.github.com/repos/cBioPortal/cbioportal-core/commits/${CORE_BRANCH} /dev/null
RUN git clone --branch ${CORE_BRANCH} --depth 1 https://github.com/cBioPortal/cbioportal-core.git /cbioportal-core \
    && touch /cbioportal-core/src/main/resources/application.properties \
    && mvn -f /cbioportal-core/pom.xml clean install -DskipTests \
    && mkdir -p /core && cd /core && cp /cbioportal-core/core-*.jar . && jar -xf core-*.jar scripts/ requirements.txt

FROM eclipse-temurin:21

# download system dependencies first to take advantage of docker caching
RUN apt-get update; apt-get install -y --no-install-recommends \
        build-essential \
        curl \
        python3 \
        python3-setuptools \
        python3-dev \
        python3-pip \
        unzip \
        perl \
    && rm -rf /var/lib/apt/lists/*

# install clickhouse
RUN mkdir -p /tmp && cd /tmp && curl https://clickhouse.com/ | sh && \
    ./clickhouse install --noninteractive;

# copy over core files (scripts, requirements) built from cbioportal-core
COPY --from=build-core /core/scripts /core/scripts
COPY --from=build-core /core/requirements.txt /core/requirements.txt
COPY --from=build-core /core/core-*.jar /core/core-IMPORTER.jar
RUN chmod -R a+x /core/scripts/

COPY --from=build /cbioportal/src/main/resources/db-scripts /cbioportal/db-scripts
COPY --from=build /cbioportal/test/test_data/study_es_0 /cbioportal/test/study_es_0

RUN pip3 install --break-system-packages -r /core/requirements.txt

# put config files in this folder if you want to override config
ENV PATH="/core/scripts:/core/scripts/importer:/core/scripts/clickhouse_import_support:$PATH"
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

CMD ["sh", "-c", "java $(echo $JAVA_OPTS) -cp /cbioportal-webapp:/cbioportal-webapp/lib/* org.cbioportal.application.PortalApplication $(echo $WEBAPP_OPTS)"] 
