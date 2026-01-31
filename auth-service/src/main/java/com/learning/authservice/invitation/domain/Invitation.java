package com.learning.authservice.invitation.domain;

import com.learning.common.infra.tenant.TenantAuditingListener;
import com.learning.common.tenant.TenantAware;
import com.learning.common.tenant.TenantContext;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a pending user invitation to an organization.
 * Tenant isolation is handled via TenantDataSourceRouter.
 */
@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "idx_invitations_token", columnList = "token"),
        @Index(name = "idx_invitations_email", columnList = "email"),
        @Index(name = "idx_invitations_tenant", columnList = "tenant_id")
})
@EntityListeners(TenantAuditingListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = TenantContext.DEFAULT_TENANT;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "role_id", nullable = false, length = 64)
    private String roleId;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    @Column(name = "invited_by", nullable = false, length = 255)
    private String invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
