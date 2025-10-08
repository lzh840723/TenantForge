#!/usr/bin/env sh
set -eu
JAR_PATH=${JAR_PATH:-target/tenantforge-backend-0.1.0-SNAPSHOT.jar}
PORT_TO_USE=${PORT:-8080}
echo "USING_PORT=${PORT_TO_USE}"
exec java -Dserver.port=${PORT_TO_USE} -jar "${JAR_PATH}"

