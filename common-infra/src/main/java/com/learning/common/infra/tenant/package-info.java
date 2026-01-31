/**
 * Infrastructure components for multi-tenant support.
 * 
 * <p>This package provides servlet and JPA integration for row-level tenancy:</p>
 * <ul>
 *   <li>{@link com.learning.common.infra.tenant.TenantFilter} - Extracts tenant from headers</li>
 *   <li>{@link com.learning.common.infra.tenant.TenantAuditingListener} - Auto-populates tenantId on entities</li>
 * </ul>
 * 
 * @see com.learning.common.tenant.TenantContext
 * @see com.learning.common.tenant.TenantAware
 */
package com.learning.common.infra.tenant;
