package com.tenantforge.app.perf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;

@EnabledIfEnvironmentVariable(named = "RUN_PG_IT", matches = "(?i)true|1|yes")
class PerformancePlanIT {

    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void init() throws Exception {
        pg.start();
        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
             Statement st = c.createStatement()) {
            st.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
            execSql(st, Path.of("src/main/resources/db/migration/V1__init_schema.sql"));
            execSql(st, Path.of("../supabase/030_time_report_views.sql"));
        }
    }

    @Test
    void explain_uses_indexes_for_time_entries() throws Exception {
        UUID tenant = UUID.randomUUID();
        UUID task = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Instant s = Instant.parse("2024-01-01T10:00:00Z");
        Instant e = Instant.parse("2024-01-01T12:00:00Z");

        try (Connection c = DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
             Statement st = c.createStatement()) {
            st.execute("insert into tenants(id,name) values ('"+tenant+"','t')");
            st.execute("insert into projects(id,tenant_id,name) values ('"+UUID.randomUUID()+"','"+tenant+"','p')");
            st.execute("insert into tasks(id,tenant_id,project_id,name) values ('"+task+"','"+tenant+"','"+UUID.randomUUID()+"','t')");
            st.execute("insert into users(id,tenant_id,email,password_hash,display_name,role) values ('"+user+"','"+tenant+"','u@x','h','U','TENANT_USER')");
            st.execute("insert into time_entries(id,tenant_id,task_id,user_id,started_at,ended_at,notes) values ('"+UUID.randomUUID()+"','"+tenant+"','"+task+"','"+user+"','"+s+"','"+e+"','n')");

            String explain = single(st, "EXPLAIN SELECT * FROM time_entries WHERE user_id='"+user+"' AND started_at BETWEEN TIMESTAMP '2024-01-01 00:00:00+00' AND TIMESTAMP '2024-01-02 23:59:59+00'");
            // Expect planner to consider an index; vendor may choose bitmap or direct index scan
            boolean usesIndex = explain.contains("Index Scan") || explain.contains("Bitmap Index");
            assertThat(usesIndex).isTrue();
        }
    }

    private static String single(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) { sb.append(rs.getString(1)).append('\n'); }
            return sb.toString();
        }
    }

    private static void execSql(Statement st, Path path) throws Exception {
        String raw = Files.readString(path);
        for (String stmt : raw.split(";")) {
            String s = stmt.trim();
            if (s.isEmpty() || s.startsWith("--")) continue;
            st.execute(s);
        }
    }
}

