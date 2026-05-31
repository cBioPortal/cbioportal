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

# Build the executable boot jar (`package`; no `install`/`clean` needed in a
# fresh build stage). The example application.properties is put in place first so
# the build is self-contained: maven-resources-plugin reads it, and the user
# overrides it at runtime via spring.config.location.
ARG MAVEN_OPTS=-DskipTests
COPY . .
RUN cp src/main/resources/application.properties.EXAMPLE src/main/resources/application.properties \
    && mvn ${MAVEN_OPTS} -q package

# JRE (not JDK) runtime: this image only runs the prebuilt jar, so the JDK's
# compiler/dev tooling is not needed. Shenandoah GC is included in the standard
# Temurin JRE.
FROM eclipse-temurin:21-jre

ENV PORTAL_WEB_HOME=/cbioportal-webapp
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build /cbioportal/target/*-exec.jar ${PORTAL_WEB_HOME}/app.jar
# Shell form so $JAVA_OPTS / $WEBAPP_OPTS are expanded at runtime.
CMD ["sh", "-c", "java $JAVA_OPTS -jar $PORTAL_WEB_HOME/app.jar $WEBAPP_OPTS"]
