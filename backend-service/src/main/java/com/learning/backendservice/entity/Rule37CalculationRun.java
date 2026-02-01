package com.learning.backendservice.entity;

import com.learning.backendservice.domain.rule37.LedgerResult;
import com.learning.common.infra.tenant.TenantAuditingListener;
import com.learning.common.tenant.TenantAware;
import com.learning.common.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * JPA entity for Rule 37 calculation runs.
 * Stores ledger-based calculation results (LedgerResult[] as JSONB).
 */
@Entity
@Table(name = "rule37_calculation_runs")
@EntityListeners(TenantAuditingListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule37CalculationRun implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = TenantContext.DEFAULT_TENANT;

    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Column(name = "as_on_date", nullable = false)
    private LocalDate asOnDate;

    @Column(name = "total_interest", precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_itc_reversal", precision = 15, scale = 2)
    private BigDecimal totalItcReversal;

    @Column(name = "calculation_data", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<LedgerResult> calculationData;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}
