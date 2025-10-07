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

## Frontend Console (Vercel/Static)

The product includes a minimal, no-build static console hosted on Vercel that
exercises all backend capabilities.

- Default backend base
  - The console reads `window.BACKEND_BASE_URL` from `frontend/config.js` at
    runtime. This repository sets it to the production domain by default.
  - You can override per-visit with a query string, for example:
    `?base=https://tenantforge-production.up.railway.app` (aliases: `backend`,
    `url`).



- UI layout and usage
  - The console uses a consistent three-zone layout in each section:
    Actions → Inputs → Results. Buttons trigger requests; inputs provide
    parameters; results show the HTTP code and body (tables for lists).
  - Auth
    - Register creates a tenant and owner, returning an access/refresh token
      pair. Login also returns a token pair. Tokens are stored locally.
    - “Copy Access” copies the JWT. The client auto-refreshes on 401 when a
      refresh token is available.
  - Projects
    - List shows a table; click a row to populate inputs. Project ID is
      read‑only; Update/Delete require selecting a row. Query searches by name
      and renders a table.
  - Tasks
    - Requires a Project ID. Pick a project on the Projects page first; the ID
      is remembered and prefilled. Status values used by the UI: NEW/OPEN/CLOSED.
  - Time Entries
    - Requires a Task ID; User ID defaults to the current JWT subject. Timestamps
      must be ISO8601 (UTC recommended). A “Seed” helper can create demo rows.
  - Reports
    - Choose `period=week|month` and optional filters. JSON renders as JSON;
      CSV is fetched with Authorization and shown inline in the results area.
  - Health
    - Calls `/api/health` and shows the response.

Tips
- Backend Base normalization accepts schemeless inputs and trims trailing slash.
- If refresh fails or you are not signed in, the console will route you to Auth
  and display a clear message.

 

 

 

 

 

 

 

 

 

## Project Structure
```
backend/   Cloud API (Spring Boot)
frontend/  Vercel static console
docs/      Customer documentation & collections
supabase/  SQL views & policies (reference)
```

## Next Steps
- Tighten CORS to final frontend domains via `CORS_ALLOWED_ORIGINS`.
- Document and/or enforce `Task.status` values (UI uses NEW/OPEN/CLOSED).
- Keep status board updated in `docs/status/` and finalize demo evidence.
## Makefile
- Format: `make format`
- Lint: `make lint`
- Typecheck: `make typecheck`
- Test (unit only): `make test`
- Integration tests (requires Docker): `make test-integration`

<!-- trigger: 2025-10-06T04:04:57Z -->

<!-- backend redeploy trigger: 2025-10-06T04:54:06Z -->
