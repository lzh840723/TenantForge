package com.tenantforge.app.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;

@EnabledIfEnvironmentVariable(named = "RUN_PG_IT", matches = "(?i)true|1|yes")
class RlsIntegrationTest {

    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void init() throws Exception {
        pg.start();
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
                Statement st = c.createStatement()) {
            // enable uuid extension for the schema
            st.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");

            // Apply base schema
            execSql(st, Path.of("src/main/resources/db/migration/V1__init_schema.sql"));

            // Apply RLS policies
            execSql(st, Path.of("../supabase/010_enable_rls.sql"));
        }
    }

    @Test
    void rls_isolation_enforced() throws Exception {
        UUID t1 = UUID.randomUUID();
        UUID t2 = UUID.randomUUID();

        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
                Statement st = c.createStatement()) {
            // Insert two tenants with respective session tenant ids
            st.execute("select set_config('app.tenant_id', '" + t1 + "', false)");
            st.execute("insert into tenants(id, name) values ('" + t1 + "', 't1')");

            st.execute("select set_config('app.tenant_id', '" + t2 + "', false)");
            st.execute("insert into tenants(id, name) values ('" + t2 + "', 't2')");

            // When using t1, we must only see t1
            st.execute("select set_config('app.tenant_id', '" + t1 + "', false)");
            try (ResultSet rs = st.executeQuery("select count(*) from tenants")) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }

            // Cross-tenant write should fail (policy WITH CHECK)
            boolean failed = false;
            try {
                st.execute("insert into projects(id, tenant_id, name) values ('" + UUID.randomUUID() + "', '" + t2 + "', 'p')");
            } catch (Exception e) {
                failed = true;
            }
            assertThat(failed).isTrue();
        }
    }

    private static void execSql(Statement st, Path path) throws Exception {
        String raw = Files.readString(path);
        for (String stmt : splitSql(raw)) {
            String s = stmt.trim();
            if (s.isEmpty() || s.startsWith("--")) continue;
            st.execute(s);
        }
    }

    private static java.util.List<String> splitSql(String sql) {
        java.util.List<String> stmts = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inSingle = false;
        boolean inDollar = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = (i + 1 < sql.length()) ? sql.charAt(i + 1) : '\0';

            // Toggle dollar-quoted block $$ ... $$
            if (!inSingle && c == '$' && next == '$') {
                inDollar = !inDollar;
                sb.append("$$");
                i++; // consume next '$'
                continue;
            }

            // Toggle single-quoted string '...'
            if (!inDollar && c == '\'' ) {
                // handle escaped single quote '' inside strings
                if (inSingle && next == '\'') {
                    sb.append("''");
                    i++; // consume escaped quote
                    continue;
                }
                inSingle = !inSingle;
                sb.append(c);
                continue;
            }

            // Split on semicolon only when not inside quotes/blocks
            if (!inSingle && !inDollar && c == ';') {
                stmts.add(sb.toString());
                sb.setLength(0);
                continue;
            }

            sb.append(c);
        }
        if (sb.length() > 0) {
            stmts.add(sb.toString());
        }
        return stmts;
    }
}
