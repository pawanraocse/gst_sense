package com.learning.common.tenant;

/**
 * Interface for entities that support multi-tenancy.
 * 
 * <p>Entities implementing this interface will have their {@code tenantId}
 * automatically populated by {@link TenantAuditingListener} during persistence
 * based on the current {@link TenantContext}.</p>
 * 
 * <p>This follows the Interface Segregation Principle (ISP) - entities only
 * need to implement tenant awareness if they require it.</p>
 * 
 * @see TenantContext
 */
public interface TenantAware {

    /**
     * Gets the tenant identifier for this entity.
     * 
     * @return the tenant ID, never null for persisted entities
     */
    String getTenantId();

    /**
     * Sets the tenant identifier for this entity.
     * 
     * @param tenantId the tenant ID to set
     */
    void setTenantId(String tenantId);
}
