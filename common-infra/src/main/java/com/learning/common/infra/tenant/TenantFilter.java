package com.learning.common.infra.tenant;

import com.learning.common.constants.HeaderNames;
import com.learning.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that extracts tenant ID from request headers and populates
 * the {@link TenantContext} for the duration of the request.
 * 
 * <h3>Request Flow:</h3>
 * <pre>
 * Gateway → X-Tenant-Id header → TenantFilter → TenantContext → Services
 * </pre>
 * 
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li>Runs early in filter chain (HIGHEST_PRECEDENCE + 10) to ensure
 *       tenant context is available for all downstream components</li>
 *   <li>Uses {@link OncePerRequestFilter} to guarantee single execution</li>
 *   <li>Always clears context in finally block to prevent thread-local leaks</li>
 *   <li>Falls back to default tenant if header is missing (backward compatible)</li>
 * </ul>
 * 
 * <h3>Security Note:</h3>
 * <p>The Gateway is responsible for validating and extracting tenant ID from JWT.
 * This filter trusts the header coming from the Gateway (internal traffic only).</p>
 * 
 * @see TenantContext
 * @see HeaderNames#TENANT_ID
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String tenantId = extractTenantId(request);
            TenantContext.setCurrentTenant(tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Tenant context set: tenantId={}, path={}",
                        tenantId, request.getRequestURI());
            }

            filterChain.doFilter(request, response);

        } finally {
            // CRITICAL: Always clear to prevent memory leaks in thread pools
            TenantContext.clear();
        }
    }

    /**
     * Extracts tenant ID from the request header.
     * 
     * @param request the HTTP request
     * @return tenant ID from header, or default if not present
     */
    private String extractTenantId(HttpServletRequest request) {
        String tenantId = request.getHeader(HeaderNames.TENANT_ID);

        if (tenantId == null || tenantId.isBlank()) {
            log.trace("No tenant header present, using default");
            return TenantContext.DEFAULT_TENANT;
        }

        return tenantId.trim();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Skip tenant filter for actuator endpoints (health checks, metrics)
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}
