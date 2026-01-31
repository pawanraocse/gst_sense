package com.learning.common.tenant;

/**
 * Thread-local holder for the current tenant context.
 * 
 * <p>This class provides a centralized way to access the current tenant ID
 * within the request scope. The tenant is typically set by a servlet filter
 * (e.g., {@code TenantFilter}) at the beginning of each request and cleared
 * after the request completes.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Set by filter at request start
 * TenantContext.setCurrentTenant("tenant-123");
 * 
 * // Read anywhere in the request chain
 * String tenantId = TenantContext.getCurrentTenant();
 * 
 * // Cleared by filter at request end
 * TenantContext.clear();
 * }</pre>
 * 
 * <h3>Thread Safety:</h3>
 * <p>Uses {@link ThreadLocal} to ensure thread-safe access in servlet containers
 * where each request is handled by a separate thread.</p>
 * 
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li>Default tenant is {@code "default"} for backward compatibility</li>
 *   <li>Utility class pattern (private constructor, static methods)</li>
 *   <li>Follows Single Responsibility Principle - only manages tenant context</li>
 * </ul>
 * 
 * @see TenantAware
 */
public final class TenantContext {

    /**
     * Default tenant ID used when no tenant is explicitly set.
     */
    public static final String DEFAULT_TENANT = "default";

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current tenant ID from the thread-local context.
     * 
     * @return the current tenant ID, or {@link #DEFAULT_TENANT} if not set
     */
    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : DEFAULT_TENANT;
    }

    /**
     * Sets the current tenant ID in the thread-local context.
     * 
     * @param tenantId the tenant ID to set; if null, {@link #DEFAULT_TENANT} is used
     */
    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        } else {
            CURRENT_TENANT.set(tenantId);
        }
    }

    /**
     * Clears the tenant context from the current thread.
     * 
     * <p><strong>IMPORTANT:</strong> This must be called at the end of each request
     * to prevent memory leaks in pooled thread environments.</p>
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Checks if a tenant context is currently set.
     * 
     * @return true if a non-default tenant is set
     */
    public static boolean isSet() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null && !DEFAULT_TENANT.equals(tenant);
    }
}
