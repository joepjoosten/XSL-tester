#!/usr/bin/env bash

# SBT Launcher Script
# Downloads sbt-launch.jar if needed and runs sbt

SBT_VERSION="1.10.1"
SBT_LAUNCH_JAR="$HOME/.sbt/launchers/$SBT_VERSION/sbt-launch.jar"
SBT_LAUNCH_URL="https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/$SBT_VERSION/sbt-launch-$SBT_VERSION.jar"

# Create directory if it doesn't exist
mkdir -p "$(dirname "$SBT_LAUNCH_JAR")"

# Download sbt-launch.jar if it doesn't exist
if [ ! -f "$SBT_LAUNCH_JAR" ]; then
    echo "Downloading sbt-launch $SBT_VERSION..."
    curl -L -o "$SBT_LAUNCH_JAR" "$SBT_LAUNCH_URL"
fi

# Default JVM options
SBT_OPTS="${SBT_OPTS:--Xms512M -Xmx2G -Xss2M -XX:MaxMetaspaceSize=1G}"

# Run sbt
exec java $SBT_OPTS -jar "$SBT_LAUNCH_JAR" "$@"
