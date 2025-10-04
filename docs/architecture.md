# TenantForge Architecture

Overview
- Backend: Spring Boot (Java 21), JPA/Hibernate, JWT Security, Micrometer/Actuator.
- Database: PostgreSQL (Supabase). RLS policies restrict data per-tenant via session GUC `app.tenant_id`.
- Frontend: Static landing page (Vercel) [placeholder].
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

