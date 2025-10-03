# TenantForge Starter

Multi-tenant project & time tracking starter kit built with Spring Boot, PostgreSQL, and Supabase-managed RLS. This repository provides:

- A Spring Boot backend skeleton ready for multi-tenant features.
- Supabase SQL scripts to enable row-level security.
- Deployment scripts for Koyeb (API) and Vercel (static site).
- Optional local Docker Compose stack for development parity.

## Prerequisites
- Java 21
- Maven 3.9+
- Supabase account (with database + connection pool enabled)
- Koyeb CLI (`brew install koyeb/tap/koyeb`)
- Vercel CLI (`npm i -g vercel`)
- Docker (only if you want to run the compose stack locally)

## Environment Variables
Sensitive values are stored in `local_notes/.env` and should **not** be committed. Load them when needed (the file uses `#` comments so it can be sourced safely):

```bash
set -a
source local_notes/.env
set +a
```

Common variables:
- `KOYEB_API_KEY`
- `DIRECT_CONNECTION` – Supabase direct URL, used for migrations (requires `sslmode=require`)
- `TRANSACTION_POOLER` – Supabase pooled URL, used by the application/runtime
- `VERCEL_TOKEN`
- `GHCR_USERNAME`, `GHCR_TOKEN` (for pushing Docker images)

Derived runtime variables (pointing at the pooler):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Run Tests Against Supabase
Tests expect a reachable cloud database (no local Docker required). Use the Supabase transaction pooler for the application datasource and provide the non-jdbc URL to satisfy the guard rails:

```bash
SPRING_DATASOURCE_URL="jdbc:postgresql://<pooler-host>:<port>/<db>?sslmode=require" \
SPRING_DATASOURCE_USERNAME="<pooler-username>" \
SPRING_DATASOURCE_PASSWORD="<pooler-password>" \
DIRECT_CONNECTION="postgresql://<pooler-username>:<pooler-password>@<pooler-host>:<port>/<db>" \
mvn -f backend/pom.xml clean verify
```

The test profile disables Flyway to avoid version skew; run migrations separately via `psql` when needed.

## Supabase Setup
Follow `supabase/README.md` to apply Flyway schema and row-level security policies. Example:

```bash
# Using the direct connection string (not the pooler)
psql "$DIRECT_CONNECTION" -f backend/src/main/resources/db/migration/V1__init_schema.sql
psql "$DIRECT_CONNECTION" -f supabase/010_enable_rls.sql
```

## Deploy to Koyeb

```bash
export SPRING_DATASOURCE_URL="$TRANSACTION_POOLER" # converted to JDBC inside the script
export SPRING_DATASOURCE_USERNAME="<pooler-username>"
export SPRING_DATASOURCE_PASSWORD="<pooler-password>"
export GHCR_USERNAME="<your-ghcr-username>"
export GHCR_TOKEN="<github-personal-access-token>"

./scripts/koyeb-deploy.sh
```

The script logs in with your token, builds the Docker image, pushes to GHCR, and triggers a rolling deploy on Koyeb.

## Deploy Static Site to Vercel

```bash
./scripts/vercel-deploy.sh
```

Optional scope/project flags can be provided via `VERCEL_ORG_ID` and `VERCEL_PROJECT_ID` environment variables.

## Optional Local Compose Stack

```bash
# Build backend jar
( cd backend && mvn clean package )

# Start API + Postgres
docker compose up --build

# Health check
curl http://localhost:8080/api/health
```

## Project Structure
```
backend/                 Spring Boot service
  ├── Dockerfile
  ├── pom.xml
  └── src/
frontend/                Static landing page for Vercel
supabase/                SQL scripts for RLS policies
scripts/                 Deployment helpers
local_notes/             Internal playbooks (.env stored here)
```

## Next Steps
- Implement authentication, RBAC, and tenant context interceptors.
- Add integration tests that validate RLS behaviour and audit logging.
- Build CI/CD pipelines (GitHub Actions) to automate build + deploy.
