#!/usr/bin/env bash
set -euo pipefail

: "${KOYEB_API_KEY?KOYEB_API_KEY not set}"
: "${SPRING_DATASOURCE_URL?SPRING_DATASOURCE_URL not set}"
: "${SPRING_DATASOURCE_USERNAME?SPRING_DATASOURCE_USERNAME not set}"
: "${SPRING_DATASOURCE_PASSWORD?SPRING_DATASOURCE_PASSWORD not set}"
: "${GHCR_USERNAME?GHCR_USERNAME not set}"
: "${GHCR_TOKEN?GHCR_TOKEN not set}"

APP_NAME=${KOYEB_APP_NAME:-tenantforge-api}
SERVICE_NAME=${KOYEB_SERVICE_NAME:-tenantforge-service}
IMAGE_TAG=ghcr.io/${GHCR_USERNAME}/tenantforge-backend:latest

jdbc_url() {
  local url="$1"
  if [[ "$url" == jdbc:postgresql://* ]]; then
    printf '%s' "$url"
    return
  fi
  if [[ "$url" != postgresql://* ]]; then
    printf 'SPRING_DATASOURCE_URL must be jdbc:postgresql:// or postgresql:// format\n' >&2
    exit 1
  fi
  local without_scheme=${url#postgresql://}
  local host_and_db=${without_scheme#*@}
  local host_port=${host_and_db%%/*}
  local db_path=/${host_and_db#*/}
  local host=${host_port%%:*}
  local port=${host_port##*:}
  if [[ "$host" == "$port" ]]; then
    port=5432
  fi
  local query=""
  if [[ "$db_path" == *"?"* ]]; then
    query="${db_path#*?}"
    db_path="/${db_path%%\?*}"
  fi
  local jdbc="jdbc:postgresql://${host}:${port}${db_path}"
  if [[ -z "$query" ]]; then
    jdbc+="?sslmode=require"
  elif [[ "$query" == *"sslmode="* ]]; then
    jdbc+="?${query}"
  else
    jdbc+="?${query}&sslmode=require"
  fi
  printf '%s' "$jdbc"
}

JDBC_URL=$(jdbc_url "$SPRING_DATASOURCE_URL")

docker login ghcr.io -u "$GHCR_USERNAME" -p "$GHCR_TOKEN"
docker build -t "$IMAGE_TAG" backend

docker push "$IMAGE_TAG"

if ! koyeb app get "$APP_NAME" --token "$KOYEB_API_KEY" >/dev/null 2>&1; then
  koyeb app create "$APP_NAME" --token "$KOYEB_API_KEY"
fi

koyeb service deploy "$SERVICE_NAME" \
  --token "$KOYEB_API_KEY" \
  --app "$APP_NAME" \
  --container image="$IMAGE_TAG" \
  --env SPRING_DATASOURCE_URL="$JDBC_URL" \
  --env SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  --env SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  --env JWT_SECRET="${JWT_SECRET:-change-me-change-me-change-me-change-me-123456}" \
  --ports http:8080 \
  --strategy rolling

echo "Deployment triggered for ${APP_NAME}/${SERVICE_NAME} using image ${IMAGE_TAG}"
