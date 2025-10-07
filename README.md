# TenantForge Starter

Multi-tenant project & time tracking starter kit built with Spring Boot, PostgreSQL, and Supabase-managed RLS. This repository provides:

- A Spring Boot backend skeleton ready for multi-tenant features.
- Supabase SQL scripts to enable row-level security.
- Deployment playbooks for Railway (API) and Vercel (static site).
- Optional local Docker Compose stack for development parity.

## Docs
- Postman collection: `docs/postman/TenantForge.postman_collection.json`
- Demo script: `docs/demo_script.md`
- Status dashboard: `docs/status/status.md` (JSON/board/burndown in same folder)
- Changes log: `changes/`
- Architecture: `docs/architecture.md`
- API Quickstart: `docs/api_quickstart.md`
- Evidence: `docs/evidence/` (reports, coverage, PG IT)
- Demo assets: `docs/demo/`

## API
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

## Prerequisites
- Java 21
- Maven 3.9+
- Supabase account (with database + connection pool enabled)
- Railway CLI (`brew install railway`)
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
- `DIRECT_CONNECTION` – Supabase direct URL, used for migrations (requires `sslmode=require`)
- `TRANSACTION_POOLER` – Supabase pooled URL, used by the application/runtime
- `RENDER_API_KEY`
- `VERCEL_TOKEN`

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

## Deploy to Railway

Railway can build a Spring Boot service directly from this repository. Prepare the CLI with the required IDs and token listed in `local_notes/.env`:

```bash
set -a
source local_notes/.env
set +a

# The script reads the following variables (IDs preferred for stability):
export RAILWAY_TOKEN="$RAILWAY_TOKEN"            # Project access token
export RAILWAY_PROJECT_ID="e89e8ca8-88c1-4734-ad72-6f98730abc01"
export RAILWAY_ENVIRONMENT_ID="fa80cd3a-0a22-488b-8e2a-8d98e668568c"
export RAILWAY_SERVICE_ID="dd4fb1ea-c9f4-40b8-b303-30d0fd65faa0"

./scripts/railway-deploy.sh
```

What the helper script does:
- Validates the Supabase and JWT secrets and writes a temporary `.railway/config.json` with the IDs above.
- Synchronises the required environment variables via `railway variables --skip-deploys` so the service picks up the latest database credentials and JWT secret.
- Runs `mvn -B -DskipTests package` to produce the fat JAR and calls `railway up --ci` to trigger a deploy.

To inspect the build/deploy logs afterwards, run `railway logs --service TenantForge --environment production`. Update the IDs in `.env` if you create a new Railway project or environment.

## Operations Notes

- Console logs now include `tenant=<UUID>` thanks to the request-scoped MDC binding. This makes it easy to filter multi-tenant output when using Railway logs or other collectors.
- Railway captures streaming logs by default; use `railway logs --service TenantForge --environment production --tail` for a live feed during incidents.
- If you need structured logs for external systems, point the service at a Log Drain in the Railway dashboard—no application changes required because we emit to stdout.
- Prometheus metrics are available at `/actuator/prometheus`; Railway can scrape them directly or pipe them to Grafana Cloud via observability integrations.

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

## PG IT (Evidence)
- A manual workflow is provided to run Postgres-backed integration tests and upload artifacts (EXPLAIN plan, view aggregation sample).
- Trigger via GitHub Actions: "PG Integration Evidence" → Run workflow.
- Artifacts: `pg-it-evidence` (includes `backend/target/artifacts/**`).

## Project Structure
```
backend/                 Spring Boot service
  ├── Dockerfile
  ├── pom.xml
  └── src/
frontend/                Static landing page for Vercel
supabase/                SQL scripts for RLS policies
docs/                    Postman collection and demo script
scripts/                 Deployment helpers
local_notes/             Internal playbooks (.env stored here)
```

## Next Steps
- Tighten CORS to final frontend domains via `CORS_ALLOWED_ORIGINS`.
- Add end-to-end tests: Auth → Projects → Tasks → Time Entries → Reports (incl. 401 auto-refresh flow).
- Document and/or enforce `Task.status` values (UI uses NEW/OPEN/CLOSED). Option: migrate to enum with validation.
- Finalize demo evidence and close the acceptance checklist in `docs/status/`.
- Ensure deployment secrets are present so CI can auto-deploy (Railway and Vercel tokens/IDs).
## Makefile
- Format: `make format`
- Lint: `make lint`
- Typecheck: `make typecheck`
- Test (unit only): `make test`
- Integration tests (requires Docker): `make test-integration`

<!-- trigger: 2025-10-06T04:04:57Z -->

<!-- backend redeploy trigger: 2025-10-06T04:54:06Z -->
