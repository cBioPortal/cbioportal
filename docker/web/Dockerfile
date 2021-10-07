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
FROM maven:3-openjdk-11 as build
COPY $PWD /cbioportal
WORKDIR /cbioportal
ARG MAVEN_OPTS=-DskipTests
RUN mvn ${MAVEN_OPTS} clean install
RUN mkdir -p portal/target/dependency && (cd portal/target/dependency; jar -xf ../*.jar)

FROM shipilev/openjdk:11

ENV PORTAL_WEB_HOME=/cbioportal-webapp

# add exploded Spring Boot jar contents to image
# See: https://spring.io/guides/topicals/spring-boot-docker/
ARG DEPENDENCY=/cbioportal/portal/target/dependency
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib        $PORTAL_WEB_HOME/lib/
COPY --from=build ${DEPENDENCY}/META-INF            $PORTAL_WEB_HOME/META-INF/
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes    $PORTAL_WEB_HOME/

RUN mkdir -p $PORTAL_WEB_HOME

CMD ["java ${JAVA_OPTS}", "-cp", "/cbioportal-webapp:/cbioportal-webapp/lib/*", "org.cbioportal.PortalApplication ${WEBAPP_OPTS}"]
