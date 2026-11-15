#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper Startup Batch script, version 3.3.2
#
# Optional ENV vars
# -----------------
#   JAVA_HOME - location of a valid JDK home dir
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
CYGWIN*) cygwin=true ;;
Darwin*) darwin=true ;;
MINGW*) mingw=true ;;
esac

## resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' >/dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done

PRG_DIR="$(cd -P "$(dirname "$PRG")" && pwd)"

# Reset CDPATH if set
[ -n "$CDPATH" ] && unset CDPATH

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    # IBM's JDK on AIX uses strange locations for the executables
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ]; then
    echo "The JAVA_HOME environment variable is not defined correctly, so cannot run this program." >&2
    echo "JAVA_HOME is set to \"$JAVA_HOME\", but \"$JAVACMD\" does not exist." >&2
    exit 1
  fi
else
  JAVACMD="java"
  which java >/dev/null 2>&1 || {
    echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
    exit 1
  }
fi

# Locate the maven wrapper properties file
WRAPPER_PROP_FILE="$PRG_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROP_FILE" ]; then
  echo "Error: Cannot find $WRAPPER_PROP_FILE" >&2
  exit 1
fi

# Extract distributionUrl from properties
DIST_URL="$(sed -n '/^distributionUrl=/ { s/^distributionUrl=//; s/\r$//; p; }' "$WRAPPER_PROP_FILE")"

if [ -z "$DIST_URL" ]; then
  echo "Error: distributionUrl not found in $WRAPPER_PROP_FILE" >&2
  exit 1
fi

# Calculate distribution name and hash
DIST_NAME="$(basename "$DIST_URL")"
DIST_HASH="$(printf "%s" "$DIST_URL" | (md5sum 2>/dev/null || md5 -q 2>/dev/null || shasum 2>/dev/null) | awk '{print $1}')"

# Target directory for distribution
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
WRAPPER_DIR="$MAVEN_USER_HOME/wrapper/dists/$DIST_HASH"
DIST_DIR="$WRAPPER_DIR/${DIST_NAME%.zip}"

if [ ! -d "$DIST_DIR" ]; then
  mkdir -p "$WRAPPER_DIR"
  ZIP_FILE="$WRAPPER_DIR/$DIST_NAME"

  if [ ! -f "$ZIP_FILE" ]; then
    echo "Downloading Maven from $DIST_URL..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL -o "$ZIP_FILE" "$DIST_URL" || {
        echo "Error: Failed to download $DIST_URL via curl" >&2
        rm -f "$ZIP_FILE"
        exit 1
      }
    elif command -v wget >/dev/null 2>&1; then
      wget -q -O "$ZIP_FILE" "$DIST_URL" || {
        echo "Error: Failed to download $DIST_URL via wget" >&2
        rm -f "$ZIP_FILE"
        exit 1
      }
    else
      echo "Error: Neither curl nor wget was found to download Maven." >&2
      exit 1
    fi
  fi

  echo "Unpacking Maven to $WRAPPER_DIR..."
  if command -v unzip >/dev/null 2>&1; then
    unzip -qo "$ZIP_FILE" -d "$WRAPPER_DIR"
  elif command -v tar >/dev/null 2>&1; then
    tar -xf "$ZIP_FILE" -C "$WRAPPER_DIR"
  else
    "$JAVACMD" -jar "$ZIP_FILE" -d "$WRAPPER_DIR"
  fi

  # Identify unpacked directory
  UNPACKED_DIR="$(find "$WRAPPER_DIR" -mindepth 1 -maxdepth 1 -type d ! -name ".*" | head -n 1)"
  if [ -n "$UNPACKED_DIR" ] && [ "$UNPACKED_DIR" != "$DIST_DIR" ]; then
    mv "$UNPACKED_DIR" "$DIST_DIR"
  fi
  rm -f "$ZIP_FILE"
fi

MAVEN_HOME="$DIST_DIR"
chmod +x "$MAVEN_HOME/bin/mvn" 2>/dev/null

exec "$MAVEN_HOME/bin/mvn" "$@"
