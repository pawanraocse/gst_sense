package com.learning.backendservice.domain.rule37;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Value object representing a Rule 37 interest calculation row.
 * Maps to MVP {@code InterestRow} type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRow {

    private String supplier;
    private LocalDate purchaseDate;
    private LocalDate paymentDate;  // null for UNPAID
    private double principal;
    private int delayDays;
    private double itcAmount;
    private double interest;
    private InterestStatus status;

    public enum InterestStatus {
        PAID_LATE,
        UNPAID
    }
}
