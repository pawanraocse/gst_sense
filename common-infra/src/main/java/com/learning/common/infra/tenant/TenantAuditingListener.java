package com.learning.common.infra.tenant;

import com.learning.common.tenant.TenantAware;
import com.learning.common.tenant.TenantContext;
import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JPA entity listener that automatically populates {@code tenantId} on
 * {@link TenantAware} entities before persistence.
 * 
 * <h3>Usage:</h3>
 * <p>Add this listener to entities that implement {@link TenantAware}:</p>
 * <pre>{@code
 * @Entity
 * @EntityListeners(TenantAuditingListener.class)
 * public class MyEntity implements TenantAware {
 *     private String tenantId;
 *     // ...
 * }
 * }</pre>
 * 
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>On {@code @PrePersist}: Sets tenantId from {@link TenantContext} if not already set</li>
 *   <li>Respects explicitly set tenantId (does not override)</li>
 *   <li>Falls back to default tenant if context is not set</li>
 * </ul>
 * 
 * <h3>Design Principles:</h3>
 * <ul>
 *   <li><strong>Single Responsibility:</strong> Only handles tenant auditing</li>
 *   <li><strong>Open/Closed:</strong> Works with any TenantAware entity</li>
 *   <li><strong>Dependency Inversion:</strong> Depends on TenantAware interface</li>
 * </ul>
 * 
 * @see TenantAware
 * @see TenantContext
 */
@Component
public class TenantAuditingListener {

    private static final Logger log = LoggerFactory.getLogger(TenantAuditingListener.class);

    /**
     * Sets the tenant ID on new entities before persistence.
     * 
     * <p>Called by JPA lifecycle before INSERT. Only sets tenantId if:
     * <ul>
     *   <li>Entity implements {@link TenantAware}</li>
     *   <li>Entity's tenantId is currently null</li>
     * </ul>
     * 
     * @param entity the entity being persisted
     */
    @PrePersist
    public void setTenantOnCreate(Object entity) {
        if (!(entity instanceof TenantAware tenantAware)) {
            return;
        }

        if (tenantAware.getTenantId() != null) {
            // Tenant already set explicitly - respect it
            log.trace("TenantId already set on entity: {}", tenantAware.getTenantId());
            return;
        }

        String currentTenant = TenantContext.getCurrentTenant();
        tenantAware.setTenantId(currentTenant);

        if (log.isDebugEnabled()) {
            log.debug("Auto-populated tenantId={} on entity={}",
                    currentTenant, entity.getClass().getSimpleName());
        }
    }
}
