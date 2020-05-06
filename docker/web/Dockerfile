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
#
# WARNING: Be careful about publishing images generated like this publicly
# because your .git folder is exposed in the build step. We are not sure if
# this is a security risk: stackoverflow.com/questions/56278325
FROM maven:3-openjdk-11 as build
COPY $PWD /cbioportal
WORKDIR /cbioportal
RUN mvn -DskipTests clean install

FROM shipilev/openjdk-shenandoah:11

ENV PORTAL_WEB_HOME=/cbioportal-webapp

COPY --from=build /cbioportal/portal/target/cbioportal*.war /app.war
COPY --from=build /cbioportal/portal/target/dependency/webapp-runner.jar /webapp-runner.jar

RUN mkdir -p $PORTAL_WEB_HOME
RUN cd $PORTAL_WEB_HOME && jar -xvf /app.war

CMD ["/bin/sh", "-c", "/usr/bin/java ${JAVA_OPTS} -jar /webapp-runner.jar ${WEBAPP_OPTS} ${PORTAL_WEB_HOME}"]
