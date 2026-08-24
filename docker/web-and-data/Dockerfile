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
# NOTE: the .git folder is included in the build stage (the git-commit-id Maven
# plugin needs it), but excluded from the final image. No confidential
# information is exposed. (see: stackoverflow.com/questions/56278325)
ARG CORE_BRANCH=main

FROM maven:3-eclipse-temurin-21 AS build

WORKDIR /cbioportal

# Resolve Maven dependencies first, in their own layer keyed only on pom.xml +
# version.sh. As long as the dependency set is unchanged this layer (and the
# downloaded ~/.m2 contents baked into it) is restored from cache, so changing
# application source does not trigger a re-download. This layer-based caching is
# what the GitHub Actions `cache-from/to: type=gha` setup persists across CI
# runs; a BuildKit `--mount=type=cache` for ~/.m2 is intentionally NOT used here
# because that cache is builder-local and would be empty on every ephemeral CI
# runner, forcing a full re-download whenever source changes.
COPY pom.xml version.sh ./
RUN mvn -q dependency:go-offline --fail-never

# Copy the source and build the JAR with the example application.properties; the
# user overrides these application.properties at runtime, injected via
# spring.config.location (see the cbioportal-docker-compose repo).
COPY . .
# `package` (not `install`) is enough to produce the executable boot jar and
# avoids the extra install-to-local-repo step. The exploded jar layout lets the
# final image cache dependency libs separately from application classes
# (see: https://spring.io/guides/topicals/spring-boot-docker/).
RUN cp src/main/resources/application.properties.EXAMPLE src/main/resources/application.properties \
    && mvn -q package -DskipTests \
    && mkdir -p target/dependency \
    && (cd target/dependency && jar -xf ../*-exec.jar)

FROM maven:3-eclipse-temurin-21 AS build-core
ARG CORE_BRANCH
# This invalidates the Docker cache iff the latest commit hash for the CORE_BRANCH has changed
ADD https://api.github.com/repos/cBioPortal/cbioportal-core/commits/${CORE_BRANCH} /dev/null
# We touch application.properties here just to get cbioportal-core building
# Later we will inject the user-provided application.properties using 'jar uf ..', see the cbioportal-docker-compose repo
RUN git clone --branch ${CORE_BRANCH} --depth 1 https://github.com/cBioPortal/cbioportal-core.git /cbioportal-core \
    && touch /cbioportal-core/src/main/resources/application.properties \
    && mvn -f /cbioportal-core/pom.xml clean install -DskipTests \
    && mkdir -p /core && cd /core && cp /cbioportal-core/core-*.jar . && jar -xf core-*.jar scripts/ requirements.txt

# JRE (not JDK) runtime: the final image only runs prebuilt jars, so the JDK's
# compiler/dev tooling is not needed.
FROM eclipse-temurin:21-jre

# Runtime system dependencies. Kept in their own early layer so they stay cached
# across application source changes. Build-only toolchain (build-essential,
# python3-dev) is intentionally NOT installed here -- it is added and removed
# within the pip layer below so it never ends up in the final image.
RUN apt-get update && apt-get install -y --no-install-recommends \
        curl \
        python3 \
        python3-setuptools \
        python3-pip \
        unzip \
        perl \
    && rm -rf /var/lib/apt/lists/*

# Install the ClickHouse client, pinned to a stable LTS. Only the
# `clickhouse client` subcommand is needed (to talk to the separate ClickHouse
# server during data import); no server or clickhouse-local runs in this image.
# The pinned static binary keeps the build reproducible and works on amd64/arm64.
ARG CLICKHOUSE_VERSION=25.8.24.21
RUN ARCH="$(dpkg --print-architecture)" \
    && curl -fsSL "https://packages.clickhouse.com/tgz/lts/clickhouse-common-static-${CLICKHOUSE_VERSION}-${ARCH}.tgz" -o /tmp/clickhouse.tgz \
    && tar -xzf /tmp/clickhouse.tgz -C /tmp \
    && install -m 755 "/tmp/clickhouse-common-static-${CLICKHOUSE_VERSION}/usr/bin/clickhouse" /usr/bin/clickhouse \
    && rm -rf /tmp/clickhouse.tgz "/tmp/clickhouse-common-static-${CLICKHOUSE_VERSION}"

# copy over core files (scripts, requirements) built from cbioportal-core
COPY --from=build-core /core/scripts /core/scripts
COPY --from=build-core /core/core-*.jar /core/core-IMPORTER.jar
RUN chmod -R a+x /core/scripts/

# Install the Python import dependencies. build-essential + python3-dev are only
# needed to compile any sdist wheels during pip install, so they are installed
# and purged within this single layer -- the compiler toolchain never lands in
# the final image.
COPY --from=build-core /core/requirements.txt /core/requirements.txt
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential python3-dev \
    && pip3 install --break-system-packages --no-cache-dir -r /core/requirements.txt \
    && apt-get purge -y --auto-remove build-essential python3-dev \
    && rm -rf /var/lib/apt/lists/*

# put config files in this folder if you want to override config
ENV PATH="/core/scripts:/core/scripts/importer:/core/scripts/clickhouse_import_support:$PATH"
ENV PORTAL_WEB_HOME=/cbioportal-webapp
ENV PORTAL_HOME=/cbioportal
ENV JAVA_OPTS="-Xms2G -Xmx8G"
ENV WEBAPP_OPTS=

# add exploded Spring Boot jar contents to image, most-volatile last so that a
# source-only change only rebuilds the classes layer (lib/ and META-INF/ stay
# cached). See: https://spring.io/guides/topicals/spring-boot-docker/
ARG DEPENDENCY=/cbioportal/target/dependency
RUN mkdir -p $PORTAL_WEB_HOME
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib                $PORTAL_WEB_HOME/lib/
COPY --from=build ${DEPENDENCY}/META-INF                    $PORTAL_WEB_HOME/META-INF/
COPY --from=build /cbioportal/src/main/resources/db-scripts /cbioportal/db-scripts
COPY --from=build /cbioportal/test/test_data/study_es_0     /cbioportal/test/study_es_0
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes            $PORTAL_WEB_HOME/

# Launch via the exploded classpath using the boot jar's Start-Class
# (org.cbioportal.PortalApplication, per the jar's MANIFEST).
CMD ["sh", "-c", "java $(echo $JAVA_OPTS) -cp /cbioportal-webapp:/cbioportal-webapp/lib/* org.cbioportal.PortalApplication $(echo $WEBAPP_OPTS)"]
