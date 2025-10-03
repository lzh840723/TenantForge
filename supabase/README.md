# Supabase Setup

1. Export credentials locally (values stored in `local_notes/.env`):
   ```bash
   export DIRECT_CONNECTION="$DIRECT_CONNECTION"
   export TRANSACTION_POOLER="$TRANSACTION_POOLER"
   ```
2. Apply schema (Flyway migration `V1__init_schema.sql`) using the **direct** connection (not the pooler):
   ```bash
   psql "$DIRECT_CONNECTION" -f backend/src/main/resources/db/migration/V1__init_schema.sql
   ```
3. Enable RLS policies:
   ```bash
   psql "$DIRECT_CONNECTION" -f supabase/010_enable_rls.sql
   ```
4. Create service role for the application (example):
   ```sql
   -- Run inside psql session connected via DIRECT_CONNECTION
   create role tenantforge_app with login password 'REPLACE_ME';
   grant usage on schema public to tenantforge_app;
   grant select, insert, update, delete on all tables in schema public to tenantforge_app;
   alter default privileges in schema public grant select, insert, update, delete on tables to tenantforge_app;
   ```
5. Application runtime uses the **transaction pooler**. Convert it to JDBC when exporting env vars:
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:postgresql://<pooler-host>:<port>/<db>?sslmode=require"
   export SPRING_DATASOURCE_USERNAME="<pooler-username>"
   export SPRING_DATASOURCE_PASSWORD="<pooler-password>"
   ```
6. Store connection strings as secrets:
   - Koyeb: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - GitHub Actions (for CI): same as above.
