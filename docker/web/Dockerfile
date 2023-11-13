# This is the web only image of cBioPortal. Useful when you would like to run a
# small image without all data import/validate related scripts and
# dependencies. It includes the ShenandoahGC
# (https://wiki.openjdk.java.net/display/shenandoah/Main), an experimental
# garbage collector which in our experience works better in a cloud environment
# where you have multiple containers running on a single node. It gives unused
# memory back to the system allowing other containers to utilize it. Enable
# this GC with JAVA_OPTS=-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC.
#
# Use from root directory of repo like:
#
# docker build -f docker/web/Dockerfile -t cbioportal-container:web-shenandoah-tag-name .
#
# WARNING: the shendoah image is a nightly, untested, experimental build. If
# you want to use an official openjdk image instead use the web-and-data image.
FROM maven:3-eclipse-temurin-21 as build
COPY $PWD /cbioportal
WORKDIR /cbioportal
ARG MAVEN_OPTS=-DskipTests
# Use quite output so we can see the build steps easily
RUN mvn ${MAVEN_OPTS} clean install -q
FROM eclipse-temurin:21

ENV PORTAL_WEB_HOME=/cbioportal-webapp
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build /cbioportal/target/*-exec.jar ${PORTAL_WEB_HOME}/app.jar
CMD ["java ${JAVA_OPTS}", "-jar", "${PORTAL_WEB_HOME}/app.jar"]
