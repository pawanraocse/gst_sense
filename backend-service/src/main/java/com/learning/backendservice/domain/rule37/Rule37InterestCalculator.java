package com.learning.backendservice.domain.rule37;

import com.learning.backendservice.domain.ledger.LedgerEntry;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for Rule 37 (180-day ITC reversal) interest calculation.
 * Pure domain logic: FIFO matching, ITC/interest formulas.
 */
public interface Rule37InterestCalculator {

    /**
     * Computes Rule 37 interest and ITC reversal for the given ledger entries.
     *
     * @param entries  sorted ledger entries (PURCHASE and PAYMENT per supplier)
     * @param asOnDate calculation date (for UNPAID delay)
     * @return calculation summary with totals and detail rows
     */
    CalculationSummary calculate(List<LedgerEntry> entries, LocalDate asOnDate);
}
