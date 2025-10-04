package com.tenantforge.app.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantAwareDataSourceTest {

    private final DataSource delegate = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement setConfigStatement = mock(PreparedStatement.class);
    private final Statement resetStatement = mock(Statement.class);

    @AfterEach
    void clean() {
        TenantContextHolder.clear();
    }

    @Test
    void setsTenantIdOnConnectionAcquireAndResetsOnClose() throws Exception {
        UUID tenantId = UUID.randomUUID();
        TenantContextHolder.setTenantId(tenantId);

        when(delegate.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("select set_config('app.tenant_id', ?, false)"))
                .thenReturn(setConfigStatement);
        when(connection.createStatement()).thenReturn(resetStatement);

        try (Connection wrapped = new TenantAwareDataSource(delegate).getConnection()) {
            assertThat(wrapped).isNotNull();
        }

        verify(setConfigStatement).setString(1, tenantId.toString());
        verify(setConfigStatement).execute();
        verify(setConfigStatement).close();

        verify(resetStatement, times(1)).execute("reset app.tenant_id");
        verify(resetStatement).close();
    }

    @Test
    void skipsTenantBindingWhenContextMissing() throws Exception {
        when(delegate.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(resetStatement);

        try (Connection wrapped = new TenantAwareDataSource(delegate).getConnection()) {
            assertThat(wrapped).isNotNull();
        }

        verify(connection, never()).prepareStatement(any());
        verify(resetStatement, times(2)).execute("reset app.tenant_id");
    }
}
