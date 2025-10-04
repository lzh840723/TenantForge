package com.tenantforge.app.config;

import com.tenantforge.app.tenant.TenantAwareDataSource;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TenantDataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnBean(DataSource.class)
    public DataSource tenantAwareDataSource(DataSource dataSource) {
        if (dataSource instanceof TenantAwareDataSource) {
            return dataSource;
        }
        return new TenantAwareDataSource(dataSource);
    }
}
