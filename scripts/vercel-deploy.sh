#!/usr/bin/env bash
set -euo pipefail

: "${VERCEL_TOKEN?VERCEL_TOKEN not set}"
PROJECT_DIR=${VERCEL_PROJECT_DIR:-frontend}
VERCEL_ORG=${VERCEL_ORG_ID:-}
VERCEL_PROJECT=${VERCEL_PROJECT_ID:-}

ARGS=("--token" "$VERCEL_TOKEN" "--cwd" "$PROJECT_DIR")
if [[ -n "$VERCEL_ORG" ]]; then
  ARGS+=("--scope" "$VERCEL_ORG")
fi
if [[ -n "$VERCEL_PROJECT" ]]; then
  ARGS+=("--project" "$VERCEL_PROJECT")
fi

vercel deploy "${ARGS[@]}" --prebuilt --prod
