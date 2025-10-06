#!/usr/bin/env bash
set -euo pipefail

# TenantForge API demo runner
# Usage: BASE_URL=https://<domain> ./scripts/demo_run.sh
# or:    ./scripts/demo_run.sh https://<domain>

BASE_URL=${BASE_URL:-${1:-}}
if [ -z "${BASE_URL}" ]; then
  echo "Usage: BASE_URL=https://your.api ./scripts/demo_run.sh" >&2
  exit 2
fi

need() { command -v "$1" >/dev/null 2>&1 || { echo "Missing dependency: $1" >&2; exit 3; }; }
need curl
need jq

ts() { date -u +"%Y%m%dT%H%M%SZ"; }
RUN_ID="demo_$(ts)"
OUT_DIR="docs/evidence/api_demo/${RUN_ID}"
mkdir -p "$OUT_DIR"

log() { echo "[demo] $*"; }

log "Health check ${BASE_URL}/api/health"
curl -fsS "${BASE_URL}/api/health" | tee "${OUT_DIR}/health.json" >/dev/null

# Unique email per run
EMAIL="demo+$(ts)@tenantforge.dev"
PASS="P@ssw0rd123"

log "Register tenant + owner"
REG_PAYLOAD=$(jq -n --arg t "demo-tenant" --arg e "$EMAIL" --arg p "$PASS" --arg d "Demo Owner" '{tenantName:$t,email:$e,password:$p,displayName:$d}')
REG_RESP=$(curl -fsS -H 'Content-Type: application/json' -d "$REG_PAYLOAD" "${BASE_URL}/api/auth/register")
echo "$REG_RESP" | jq '{accessToken:(.accessToken|type? // "string"), refreshToken:"<redacted>", accessExpiresIn, refreshExpiresIn}' >"${OUT_DIR}/register_sanitized.json" || true

ACCESS=$(echo "$REG_RESP" | jq -r '.accessToken')
if [ -z "$ACCESS" ] || [ "$ACCESS" = "null" ]; then
  log "Register did not return accessToken; trying login"
  LOGIN_PAYLOAD=$(jq -n --arg e "$EMAIL" --arg p "$PASS" '{email:$e,password:$p}')
  ACCESS=$(curl -fsS -H 'Content-Type: application/json' -d "$LOGIN_PAYLOAD" "${BASE_URL}/api/auth/login" | jq -r '.accessToken')
fi

if [ -z "$ACCESS" ] || [ "$ACCESS" = "null" ]; then
  echo "[demo] ERROR: could not obtain access token" >&2
  exit 4
fi

HDR=("-H" "Authorization: Bearer ${ACCESS}")

log "Create project"
curl -fsS -H 'Content-Type: application/json' "${HDR[@]}" \
  -d '{"name":"Sample Project","description":"demo"}' \
  "${BASE_URL}/api/projects" | jq . >"${OUT_DIR}/project_created.json"

log "List projects"
curl -fsS "${HDR[@]}" "${BASE_URL}/api/projects?size=10" | jq . >"${OUT_DIR}/projects_list.json"

log "Weekly time report (JSON)"
curl -fsS "${HDR[@]}" "${BASE_URL}/api/reports/time?period=week" | jq . >"${OUT_DIR}/report_week.json"

cat >"${OUT_DIR}/README.md" <<EOF
# API Demo Run
- Base URL: ${BASE_URL}
- Run ID: ${RUN_ID}
- Email: ${EMAIL}
- Steps: health → register/login → create project → list → weekly report
- Files:
  - health.json
  - register_sanitized.json
  - project_created.json
  - projects_list.json
  - report_week.json
EOF

log "Done. Evidence saved to ${OUT_DIR}"

