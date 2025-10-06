-- Enable RLS on all multi-tenant tables
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE time_entries ENABLE ROW LEVEL SECURITY;

-- Helper function to read tenant id from connection setting
CREATE OR REPLACE FUNCTION current_tenant()
RETURNS uuid AS $$
BEGIN
    RETURN current_setting('app.tenant_id', true)::uuid;
END;
$$ LANGUAGE plpgsql;

-- Policies restrict access to rows matching current tenant
CREATE POLICY tenants_isolation_policy ON tenants
    USING (id = current_tenant())
    WITH CHECK (id = current_tenant());

CREATE POLICY users_isolation_policy ON users
    USING (tenant_id = current_tenant())
    WITH CHECK (tenant_id = current_tenant());

CREATE POLICY projects_isolation_policy ON projects
    USING (tenant_id = current_tenant())
    WITH CHECK (tenant_id = current_tenant());

CREATE POLICY tasks_isolation_policy ON tasks
    USING (tenant_id = current_tenant())
    WITH CHECK (tenant_id = current_tenant());

CREATE POLICY time_entries_isolation_policy ON time_entries
    USING (tenant_id = current_tenant())
    WITH CHECK (tenant_id = current_tenant());
