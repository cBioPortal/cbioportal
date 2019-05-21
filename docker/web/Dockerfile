# This is the web only image of cBioPortal. Useful when you would like to run a
# small image without all data import/validate related scripts and
# dependencies. It includes the ShenandoahGC
# (https://wiki.openjdk.java.net/display/shenandoah/Main), an experimental
# garbage collector which in our experience works better in a cloud environment
# where you have multiple containers running on a single node. It gives unused
# memory back to the system allowing other containers to utilize it. Enable
# this GC with JAVA_OPTS=-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC.
#
# WARNING: the shendoah image is a nightly, untested, experimental build. If
# you want to use an official openjdk image instead use the web-and-data image.
#
FROM maven:3.5.4 as build
COPY $PWD /cbioportal
WORKDIR /cbioportal
RUN mvn -DskipTests clean install

FROM shipilev/openjdk-shenandoah:8
COPY --from=build /cbioportal/portal/target/cbioportal*.war /app.war
COPY --from=build /cbioportal/portal/target/dependency/webapp-runner.jar /webapp-runner.jar
CMD /usr/bin/java ${JAVA_OPTS} -jar /webapp-runner.jar ${WEBAPP_OPTS} /app.war
