package com.learning.backendservice.domain.rule37;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object representing Rule 37 result for one ledger file.
 * Maps to MVP {@code LedgerResult} type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerResult {

    private String ledgerName;
    private CalculationSummary summary;
}
