# TenantForge 0.1.0-SNAPSHOT — Release Notes (Draft)

Highlights
- Multitenant foundation: JWT auth, tenant context, RLS-compatible datasource session key `app.tenant_id`.
- Core domain: Projects, Tasks, Time Entries with pagination, filter, soft-delete.
- Reports: Weekly/Monthly time aggregation (views + API with JSON/CSV).
- Observability: Actuator health/metrics, OpenAPI UI, Prometheus registry.
- CI/CD: GitHub Actions build + deploy hooks (Railway/Vercel), coverage reporting.

Breaking Changes
- N/A for initial snapshot.

Notable Additions
- SQL
  - `backend/src/main/resources/db/migration/V1__init_schema.sql`
  - `supabase/010_enable_rls.sql` (RLS policies)
  - `supabase/020_audit_log.sql` (change log + triggers)
  - `supabase/030_time_report_views.sql` (weekly/monthly views + indexes)
- Backend
  - Controllers: Projects, Tasks, TimeEntries, Reports, Auth
  - Tenant-aware datasource + JWT filter wiring
- Tests
  - Unit + validation + selective PG IT (manual workflow)

Operations
- Manual workflow "PG Integration Evidence" runs PG-backed IT and uploads evidence artifacts.
- Use `.env` (not committed) for secrets; see README.

Known Limitations
- Controller E2E with real JWT are not enabled by default in CI.
- Diff-coverage gating is disabled by default; jacoco.xml uploaded for review.

Upgrade/Deploy Notes
- Apply SQL migrations to Supabase via direct connection; see README.
- Configure Railway env vars (datasource + JWT secret) before deploy.

Changelog Summary
- See commit history on branch `feat/backend-scaffold`.
