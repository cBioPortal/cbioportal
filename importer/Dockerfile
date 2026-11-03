# -------- Build the JAR --------
FROM maven:3-eclipse-temurin-21 AS jar_builder
WORKDIR /app

# Copy only pom first to leverage cache if deps don't change
COPY pom.xml .

# Now copy sources and build
COPY src ./src
RUN mvn clean package -DskipTests

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
COPY requirements.txt /tmp/requirements.txt
RUN "$VIRTUAL_ENV/bin/pip" install --no-cache-dir wheel \
 && "$VIRTUAL_ENV/bin/pip" install --no-cache-dir -r /tmp/requirements.txt

# Make venv first on PATH (so `python`, `pip` refer to venv)
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

# Optional convenience symlink; safe now that venv is first in PATH
RUN ln -s "$(which python3)" /usr/local/bin/python || true

# Copy the built JAR
COPY --from=jar_builder /app/core-*.jar /

# Scripts
COPY scripts/ /scripts/
RUN chmod -R a+x /scripts/

WORKDIR /scripts/
ENV PORTAL_HOME=/

# Placeholder that will be bind-mounted in runtime
RUN touch /application.properties
