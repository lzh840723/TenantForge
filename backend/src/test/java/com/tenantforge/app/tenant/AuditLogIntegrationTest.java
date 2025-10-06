package com.tenantforge.app.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;

@EnabledIfEnvironmentVariable(named = "RUN_PG_IT", matches = "(?i)true|1|yes")
class AuditLogIntegrationTest {

    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void init() throws Exception {
        pg.start();
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
             Statement st = c.createStatement()) {
            st.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
            execSql(st, Path.of("src/main/resources/db/migration/V1__init_schema.sql"));
            execSql(st, Path.of("../supabase/010_enable_rls.sql"));
            execSql(st, Path.of("../supabase/020_audit_log.sql"));
        }
    }

    @Test
    void audit_log_records_inserts_and_updates() throws Exception {
        UUID tenant = UUID.randomUUID();
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
             Statement st = c.createStatement()) {
            st.execute("select set_config('app.tenant_id', '" + tenant + "', false)");
            UUID proj = UUID.randomUUID();
            st.execute("insert into tenants(id,name) values ('"+tenant+"','t')");
            st.execute("insert into projects(id,tenant_id,name) values ('"+proj+"','"+tenant+"','p')");
            st.execute("update projects set name='p2' where id='"+proj+"'");

            int cnt;
            try (ResultSet rs = st.executeQuery("select count(*) from change_log where table_name='projects' and tenant_id='"+tenant+"'")) {
                rs.next();
                cnt = rs.getInt(1);
            }
            assertThat(cnt).isGreaterThanOrEqualTo(2);
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

            if (!inSingle && c == '$' && next == '$') {
                inDollar = !inDollar;
                sb.append("$$");
                i++;
                continue;
            }

            if (!inDollar && c == '\'') {
                if (inSingle && next == '\'') {
                    sb.append("''");
                    i++;
                    continue;
                }
                inSingle = !inSingle;
                sb.append(c);
                continue;
            }

            if (!inSingle && !inDollar && c == ';') {
                stmts.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        if (sb.length() > 0) stmts.add(sb.toString());
        return stmts;
    }
}
