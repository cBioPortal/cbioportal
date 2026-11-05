# The standalone importer image, published as cbioportal/cbioportal-core.
#
# The importer is a module of the cbioportal Maven reactor: its POM inherits from the root
# pom.xml and it depends on the shared-model module. So the build context must be the
# repository root, not importer/:
#
# docker build -f importer/Dockerfile -t cbioportal-core:tag-name .

# -------- Build the JAR --------
FROM maven:3-eclipse-temurin-21 AS jar_builder
WORKDIR /app

# Resolve dependencies in their own layer, keyed only on the POMs. Maven reads every module
# the root POM lists, so web/pom.xml is needed even though the web module isn't built here.
COPY pom.xml ./
COPY shared-model/pom.xml shared-model/
COPY web/pom.xml web/
COPY importer/pom.xml importer/
RUN mvn -q -pl importer -am dependency:go-offline --fail-never

# Copy only what the importer build needs, then build it and the modules it depends on.
# The root POM's pre-commit hook script is copied so its validate step has nothing to complain about.
COPY .githooks .githooks
COPY shared-model shared-model
COPY importer importer
RUN mvn -q -pl importer -am package -DskipTests

# -------- Runtime image --------
FROM maven:3-eclipse-temurin-21

# System deps first (single layer), then clean apt cache
RUN apt-get update && apt-get install -y --no-install-recommends \
      build-essential \
      python3 \
      python3-dev \
      python3-venv \
      python3-setuptools \
      python3-pip \
      unzip \
      perl \
  && rm -rf /var/lib/apt/lists/*

# Create and use a virtualenv to avoid PEP 668 issues
ENV VIRTUAL_ENV=/opt/venv
RUN python3 -m venv "$VIRTUAL_ENV" && "$VIRTUAL_ENV/bin/pip" install --upgrade pip

# Install Python deps into the venv
COPY importer/requirements.txt /tmp/requirements.txt
RUN "$VIRTUAL_ENV/bin/pip" install --no-cache-dir wheel \
 && "$VIRTUAL_ENV/bin/pip" install --no-cache-dir -r /tmp/requirements.txt

# Make venv first on PATH (so `python`, `pip` refer to venv)
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Optional convenience symlink; safe now that venv is first in PATH
RUN ln -s "$(which python3)" /usr/local/bin/python || true

# Copy the built JAR
COPY --from=jar_builder /app/importer/core-*.jar /

# Scripts
COPY importer/scripts/ /scripts/
RUN chmod -R a+x /scripts/

WORKDIR /scripts/
ENV PORTAL_HOME=/

# Placeholder that will be bind-mounted in runtime
RUN touch /application.properties
