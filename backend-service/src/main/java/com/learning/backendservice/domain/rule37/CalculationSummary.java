package com.learning.backendservice.domain.rule37;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Value object representing Rule 37 calculation result for a single ledger.
 * Enhanced for production with risk metrics and audit trail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationSummary {

    private double totalInterest;
    private double totalItcReversal;
    private List<InterestRow> details;

    // Production enhancements
    private int atRiskCount; // Purchases 151-180 days unpaid
    private double atRiskAmount; // Total amount at risk
    private int breachedCount; // Purchases > 180 days
    private LocalDate calculationDate; // asOnDate for audit trail

    /** Legal disclaimer for calculation assumptions */
    public static final String DISCLAIMER = "Interest calculated from invoice date. Per Section 50 + Rule 88B, actual interest "
            +
            "depends on ITC availment and utilization dates. Consult CA for precise liability.";
}
