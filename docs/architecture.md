# TenantForge Architecture

Overview
- Backend: Spring Boot (Java 21), JPA/Hibernate, JWT Security, Micrometer/Actuator.
- Database: PostgreSQL (Supabase). RLS policies restrict data per-tenant via session GUC `app.tenant_id`.
- Frontend: Static console (Vercel). Provides Auth, Projects, Tasks, Time Entries, Reports, and Health sections with a consistent three‑zone layout (Actions → Inputs → Results). Default backend base can be set in `frontend/config.js`. The console stores tokens locally and auto‑refreshes on 401; CSV reports are fetched with Authorization and rendered inline in the results area.
- CI/CD: GitHub Actions, deploy hooks for Railway/Vercel.

Key Flows
1) Authentication
   - `/api/auth/register` creates tenant + owner, returns JWT pair.
   - `/api/auth/login` validates credentials, issues tokens.
2) Multitenancy scoping
   - `JwtAuthenticationFilter` parses token, sets `TenantContextHolder`.
   - `TenantAwareDataSource` sets `set_config('app.tenant_id', <tenant>)` per-connection on Postgres.
   - RLS policies in `supabase/010_enable_rls.sql` enforce tenant visibility.
3) Domain
   - Projects/Tasks/TimeEntries: CRUD with pagination/filter/soft-delete.
4) Reports
   - Views: weekly/monthly time aggregation; API: JSON/CSV export.

Components
- Web: `ProjectController`, `TaskController`, `TimeEntryController`, `ReportController`, `AuthController`.
- Services: `ProjectService`, `TaskService`, `TimeEntryService`, `AuthService`.
- Security: `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
- Tenant: `TenantContextHolder`, `TenantAwareDataSource`, `TenantLoggingFilter`.
- Persistence: Spring Data repositories and Flyway SQL (schema), Supabase SQL (RLS/audit/views).

API Docs
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

Diagram
```mermaid
flowchart LR
  subgraph Client
    UI[Frontend / Postman]
  end

  subgraph Backend[Spring Boot]
    F[JwtAuthenticationFilter]
    C[Controllers]
    S[Services]
    DS[TenantAwareDataSource]
  end

  subgraph DB[PostgreSQL (Supabase)]
    RLS[RLS Policies\napp.tenant_id]
    AUD[Audit Triggers]
    VW[Time Views]
  end

  UI -->|Bearer JWT| F --> C --> S --> DS --> DB
  DB --> RLS
  DB --> AUD
  DB --> VW
```
