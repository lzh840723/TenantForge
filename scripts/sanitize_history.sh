#!/usr/bin/env bash
set -euo pipefail

# Remove sensitive paths from Git history using git-filter-repo (preferred) or BFG as fallback.
# This script does NOT run automatically; review before use.

SENSITIVE=(
  ".gitignore"
  "AGENTS.md"
  "index.md"
  "local_notes"
  ".env"
)

echo "[sanitizer] Sensitive paths:" "${SENSITIVE[@]}"

if ! command -v git-filter-repo >/dev/null 2>&1; then
  echo "[sanitizer] git-filter-repo not found. Install via:"
  echo "  pip install git-filter-repo    # or follow https://github.com/newren/git-filter-repo"
  echo "[sanitizer] Alternatively, install BFG and adapt commands manually."
  exit 2
fi

read -r -p "This rewrites history. Have you created a fresh backup clone? [y/N] " yn
case $yn in
  [Yy]*) ;;
  *) echo "Aborted."; exit 1;;
esac

args=()
for p in "${SENSITIVE[@]}"; do
  args+=("--path" "$p" "--path-glob" "$p/**")
done

echo "[sanitizer] Running git-filter-repo to remove sensitive paths…"
git filter-repo --invert-paths "${args[@]}"

cat <<'EOT'
[sanitizer] Next steps:
  1) Inspect the repo: git log --stat | head
  2) Force-push only if policy allows: git push --force --all && git push --force --tags
  3) Rotate any exposed secrets immediately.
EOT

