#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "${SCRIPT_DIR}/.." && pwd)

TOKEN=${RAILWAY_TOKEN:-${RAILWAY_API_KEY:-}}
if [[ -z "${TOKEN}" ]]; then
  echo "RAILWAY_TOKEN (或 RAILWAY_API_KEY) 未设置，无法调用 Railway CLI。" >&2
  exit 1
fi

if ! command -v railway >/dev/null 2>&1; then
  echo "未检测到 railway CLI，请先执行 \"brew install railway\"。" >&2
  exit 1
fi

: "${SPRING_DATASOURCE_URL?SPRING_DATASOURCE_URL 未设置}"
: "${SPRING_DATASOURCE_USERNAME?SPRING_DATASOURCE_USERNAME 未设置}"
: "${SPRING_DATASOURCE_PASSWORD?SPRING_DATASOURCE_PASSWORD 未设置}"
: "${JWT_SECRET?JWT_SECRET 未设置}"

PROJECT_ID=${RAILWAY_PROJECT_ID:?需要设置 RAILWAY_PROJECT_ID}
ENVIRONMENT_ID=${RAILWAY_ENVIRONMENT_ID:?需要设置 RAILWAY_ENVIRONMENT_ID}
SERVICE_ID=${RAILWAY_SERVICE_ID:?需要设置 RAILWAY_SERVICE_ID}
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

echo "同步 Railway 环境变量..."
if ! update_vars; then
  echo "同步环境变量失败，检查 token 或服务配置。" >&2
  exit 1
fi

echo "构建 Spring Boot JAR..."
( cd "${REPO_ROOT}/backend" && mvn -B -DskipTests package )

echo "推送代码到 Railway..."
if ! ( cd "${REPO_ROOT}/backend" && railway up --service "${SERVICE_NAME}" --environment "${ENVIRONMENT_NAME}" --ci ); then
  echo "Railway 部署命令执行失败，可运行 \"railway logs --service ${SERVICE_NAME}\" 查看详情。" >&2
  exit 1
fi

echo "部署命令已执行，如需查看日志可运行：railway logs --service ${SERVICE_NAME}"
