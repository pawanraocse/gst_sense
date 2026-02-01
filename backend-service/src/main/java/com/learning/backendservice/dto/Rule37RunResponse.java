package com.learning.backendservice.dto;

import com.learning.backendservice.domain.rule37.LedgerResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule37RunResponse {

    private Long id;
    private String filename;
    private LocalDate asOnDate;
    private BigDecimal totalInterest;
    private BigDecimal totalItcReversal;
    private OffsetDateTime createdAt;
    private String createdBy;
    private OffsetDateTime expiresAt;
    private List<LedgerResult> calculationData;  // Full data for GET by id
}
