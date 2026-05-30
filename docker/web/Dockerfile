# This is the web only image of cBioPortal. Useful when you would like to run a
# small image without all data import/validate related scripts and
# dependencies. It includes the ShenandoahGC
# (https://wiki.openjdk.java.net/display/shenandoah/Main), a low-pause garbage
# collector which in our experience works better in a cloud environment
# where you have multiple containers running on a single node. It gives unused
# memory back to the system allowing other containers to utilize it. Enable
# this GC with JAVA_OPTS="-XX:+UseShenandoahGC" (it is included in the standard
# Temurin 21 JRE used below, no experimental flag required).
#
# Use from root directory of repo like:
#
# docker build -f docker/web/Dockerfile -t cbioportal-container:web-shenandoah-tag-name .
FROM maven:3-eclipse-temurin-21 AS build

WORKDIR /cbioportal

# Resolve Maven dependencies first, in their own layer keyed only on pom.xml +
# version.sh, so that changing application source does not re-download them.
COPY pom.xml version.sh ./
RUN mvn -q dependency:go-offline --fail-never

# Build the executable boot jar. `package` is enough (no `install`/`clean` needed
# in a fresh build stage). The example application.properties is copied into place
# first so the build is self-contained: previously this image only built if the
# caller (the dockerimage.yml CI workflow) created application.properties
# beforehand, so a plain `docker build` failed in maven-resources-plugin.
ARG MAVEN_OPTS=-DskipTests
COPY . .
RUN cp src/main/resources/application.properties.EXAMPLE src/main/resources/application.properties \
    && mvn ${MAVEN_OPTS} -q package

# JRE (not JDK) runtime: this image only runs the prebuilt jar, so the JDK's
# compiler/dev tooling is not needed and just adds ~200MB. Shenandoah GC is
# included in the standard Temurin JRE.
FROM eclipse-temurin:21-jre

ENV PORTAL_WEB_HOME=/cbioportal-webapp
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build /cbioportal/target/*-exec.jar ${PORTAL_WEB_HOME}/app.jar
# Shell form so $JAVA_OPTS is expanded at runtime. The previous exec-form CMD
# (["java ${JAVA_OPTS}", ...]) did not expand the variable and treated
# "java ${JAVA_OPTS}" as a single executable name, so the container failed to start.
CMD ["sh", "-c", "java $JAVA_OPTS -jar $PORTAL_WEB_HOME/app.jar $WEBAPP_OPTS"]
