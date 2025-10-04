package com.tenantforge.app.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class TenantLoggingFilter extends OncePerRequestFilter {

    private static final String TENANT_MDC_KEY = "tenant_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            TenantContextHolder.getTenantId()
                    .ifPresentOrElse(
                            tenantId -> MDC.put(TENANT_MDC_KEY, tenantId.toString()),
                            () -> MDC.remove(TENANT_MDC_KEY));
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TENANT_MDC_KEY);
        }
    }
}
