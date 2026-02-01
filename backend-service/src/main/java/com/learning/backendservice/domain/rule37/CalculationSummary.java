package com.learning.backendservice.domain.rule37;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Value object representing Rule 37 calculation result for a single ledger.
 * Maps to MVP {@code CalculationSummary} type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationSummary {

    private double totalInterest;
    private double totalItcReversal;
    private List<InterestRow> details;
}
