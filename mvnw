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
# Maven Wrapper
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ]; then
  # check for mavenrc_pre.sh and load if exists
  if [ -n "$HOME" ] && [ -r "$HOME/.mavenrc_pre.sh" ]; then
    . "$HOME/.mavenrc_pre.sh"
  fi
fi

# Initializing a project-specific M2_HOME, if relevant
if [ -z "$MAVEN_SKIP_RC" ]; then
  # check for mavenrc_pre.sh and load if exists
  if [ -r ".mavenrc_pre.sh" ]; then
    . ".mavenrc_pre.sh"
  fi
fi

# ----------------------------------------------------------------------------
# Maven Wrapper startup script
# ----------------------------------------------------------------------------
#
# Much of this code is based on the Ant wrapper script.
#
# ----------------------------------------------------------------------------

# Resolve location of this script
PRG="$0"

# Need this for relative symlinks
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

SAVED="`pwd`"

# Use cd -P to resolve this path with existing directories
cd -P "`dirname "$PRG"`" >/dev/null
BASEDIR="`pwd`"

# Restore original working directory
cd "$SAVED" >/dev/null

# ----------------------------------------------------------------------------

#
# Function that locates the java executable.
#
# This function handles a couple of use cases:
# - JAVA_HOME is set: use that one
# - JAVA_HOME not set: check if 'java' is in PATH and use that one
# - JAVA_HOME not set, 'java' not in PATH: check standard locations for java
#
find_java() {
  if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/bin/java" ]; then
      JAVA_EXE="$JAVA_HOME/bin/java"
    fi
  else
    JAVA_EXE=java
  fi

  if [ -z "$JAVA_EXE" ]; then
    echo "Error: JAVA_HOME is not defined correctly."
    echo "  We cannot execute $JAVA_EXE"
    exit 1
  fi
}

find_java

# ----------------------------------------------------------------------------
# Find the Maven Wrapper jar
# ----------------------------------------------------------------------------

WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Verify that the maven-wrapper.jar exists
if [ ! -r "$WRAPPER_JAR" ]; then
    echo "Error: Could not find or read maven-wrapper.jar in $WRAPPER_JAR"
    exit 1
fi

# ----------------------------------------------------------------------------
# Run the Maven wrapper
# ----------------------------------------------------------------------------

exec "$JAVA_EXE" $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$BASEDIR" \
  $WRAPPER_LAUNCHER "$@"
