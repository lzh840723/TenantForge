package com.tenantforge.app.tenant;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.lang.Nullable;

public class TenantAwareDataSource extends AbstractDataSource {

    private static final String SET_CONFIG_SQL = "select set_config('app.tenant_id', ?, false)";
    private static final String RESET_SQL = "reset app.tenant_id";

    private final DataSource delegate;

    public TenantAwareDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return prepare(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return prepare(delegate.getConnection(username, password));
    }

    private Connection prepare(Connection connection) throws SQLException {
        if (isPostgres(connection)) {
            applyTenant(connection, TenantContextHolder.getTenantId());
            return wrap(connection);
        }
        // Non-Postgres (e.g., H2 in tests): skip session GUC, return raw connection
        return connection;
    }

    private static boolean isPostgres(Connection connection) {
        try {
            var meta = connection.getMetaData();
            if (meta == null) return false;
            String product = meta.getDatabaseProductName();
            return product != null && product.toLowerCase().contains("postgres");
        } catch (Exception e) {
            return false;
        }
    }

    private static void applyTenant(Connection connection, Optional<UUID> tenantId) throws SQLException {
        if (tenantId.isPresent()) {
            try (PreparedStatement ps = connection.prepareStatement(SET_CONFIG_SQL)) {
                ps.setString(1, tenantId.get().toString());
                ps.execute();
            }
        } else {
            resetTenant(connection);
        }
    }

    private static Connection wrap(Connection connection) {
        InvocationHandler handler = new TenantConnectionInvocationHandler(connection);
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, handler);
    }

    private static void resetTenant(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(RESET_SQL);
        }
    }

    private record TenantConnectionInvocationHandler(Connection delegate) implements InvocationHandler {
        @Nullable
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                try {
                    if (isPostgres(delegate)) {
                        resetTenant(delegate);
                    }
                } finally {
                    return method.invoke(delegate, args);
                }
            }
            return method.invoke(delegate, args);
        }
    }
}
