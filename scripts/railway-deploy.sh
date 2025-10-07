#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "${SCRIPT_DIR}/.." && pwd)

TOKEN=${RAILWAY_TOKEN:-${RAILWAY_API_KEY:-}}
if [[ -z "${TOKEN}" ]]; then
  echo "RAILWAY_TOKEN (or RAILWAY_API_KEY) not set; Railway CLI cannot be used." >&2
  exit 1
fi

if ! command -v railway >/dev/null 2>&1; then
  echo "railway CLI not found. Please install it (e.g., brew install railway)." >&2
  exit 1
fi

: "${SPRING_DATASOURCE_URL?SPRING_DATASOURCE_URL is required}"
: "${SPRING_DATASOURCE_USERNAME?SPRING_DATASOURCE_USERNAME is required}"
: "${SPRING_DATASOURCE_PASSWORD?SPRING_DATASOURCE_PASSWORD is required}"
: "${JWT_SECRET?JWT_SECRET is required}"

PROJECT_ID=${RAILWAY_PROJECT_ID:?RAILWAY_PROJECT_ID is required}
ENVIRONMENT_ID=${RAILWAY_ENVIRONMENT_ID:?RAILWAY_ENVIRONMENT_ID is required}
SERVICE_ID=${RAILWAY_SERVICE_ID:?RAILWAY_SERVICE_ID is required}
SERVICE_NAME=${RAILWAY_SERVICE_NAME:-TenantForge}
ENVIRONMENT_NAME=${RAILWAY_ENVIRONMENT_NAME:-production}

export RAILWAY_TOKEN="${TOKEN}"

CONFIG_DIR="${REPO_ROOT}/.railway"
mkdir -p "${CONFIG_DIR}"
cat >"${CONFIG_DIR}/config.json" <<EOF
{
  "projectId": "${PROJECT_ID}",
  "environmentId": "${ENVIRONMENT_ID}",
  "serviceId": "${SERVICE_ID}"
}
EOF

update_vars() {
  railway variables --service "${SERVICE_NAME}" \
    --environment "${ENVIRONMENT_NAME}" \
    --set "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}" \
    --set "SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}" \
    --set "SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}" \
    --set "JWT_SECRET=${JWT_SECRET}" \
    --set "CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS:-*}" \
    --skip-deploys >/dev/null
}

echo "Syncing Railway environment variables..."
if ! update_vars; then
  echo "Failed to sync environment variables. Check token/service configuration." >&2
  exit 1
fi

echo "Building Spring Boot JAR..."
( cd "${REPO_ROOT}/backend" && mvn -B -DskipTests package )

echo "Pushing code to Railway..."
if ! ( cd "${REPO_ROOT}/backend" && railway up --service "${SERVICE_NAME}" --environment "${ENVIRONMENT_NAME}" --ci ); then
  echo "Railway deploy failed. Inspect logs with: railway logs --service ${SERVICE_NAME}" >&2
  exit 1
fi

echo "Deployment triggered. To view logs: railway logs --service ${SERVICE_NAME}"
