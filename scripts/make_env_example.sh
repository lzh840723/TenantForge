#!/usr/bin/env bash
set -euo pipefail

# Generate .env.example from a source .env by removing values.
# Supports root .env or local_notes/.env.

SRC="${1:-}"
if [ -z "${SRC}" ]; then
  if [ -f .env ]; then SRC=.env; elif [ -f local_notes/.env ]; then SRC=local_notes/.env; else
    echo "No .env found. Usage: $0 [path-to-.env]"; exit 1; fi
fi

DEST=.env.example
echo "[env] Source: $SRC -> $DEST"

awk -F '=' '
  /^[ ]*#/ {print; next}
  /^[[:space:]]*$/ {print; next}
  {
    key=$1;
    sub(/^[ \t"'"'" ]+/, "", key);
    sub(/[ \t"'"'" ]+$/, "", key);
    print key "="
  }
' "$SRC" > "$DEST"

echo "[env] Wrote $DEST (values stripped). Review and add comments/defaults."

