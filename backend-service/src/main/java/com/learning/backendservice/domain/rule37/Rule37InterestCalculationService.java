package com.learning.backendservice.domain.rule37;

import com.learning.backendservice.domain.ledger.LedgerEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule 37 (180-day ITC reversal) interest calculation.
 * Port of MVP {@code gstCalculations.ts}.
 *
 * <p>Formulas (must preserve):
 * <ul>
 *   <li>ITC Amount = principal × (18 / 118)</li>
 *   <li>Interest = itcAmount × 0.18 × delayDays / 365</li>
 * </ul>
 *
 * <p>Algorithm: FIFO purchase/payment matching per supplier; PAID_LATE vs UNPAID.
 */
@Service
public class Rule37InterestCalculationService implements Rule37InterestCalculator {

    private static final double ITC_RATE = 18.0 / 118.0;
    private static final double INTEREST_RATE = 0.18;
    private static final int DAYS_THRESHOLD = 180;

    @Override
    public CalculationSummary calculate(List<LedgerEntry> entries, LocalDate asOnDate) {
        List<LedgerEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(LedgerEntry::getDate))
                .toList();

        Map<String, List<PurchaseItem>> purchaseQueues = new LinkedHashMap<>();
        Map<String, List<PaymentItem>> paymentQueues = new LinkedHashMap<>();

        for (LedgerEntry entry : sorted) {
            String supplier = entry.getSupplier();
            if (entry.getEntryType() == LedgerEntry.LedgerEntryType.PURCHASE) {
                purchaseQueues.computeIfAbsent(supplier, k -> new ArrayList<>())
                        .add(new PurchaseItem(entry.getDate(), entry.getAmount()));
            } else if (entry.getEntryType() == LedgerEntry.LedgerEntryType.PAYMENT) {
                paymentQueues.computeIfAbsent(supplier, k -> new ArrayList<>())
                        .add(new PaymentItem(entry.getDate(), entry.getAmount()));
            }
        }

        List<InterestRow> results = new ArrayList<>();

        for (Map.Entry<String, List<PurchaseItem>> e : purchaseQueues.entrySet()) {
            String supplier = e.getKey();
            List<PurchaseItem> pQueue = new ArrayList<>(e.getValue());
            List<PaymentItem> payQueue = new ArrayList<>(paymentQueues.getOrDefault(supplier, List.of()));

            // FIFO matching: PAID_LATE
            while (!pQueue.isEmpty() && !payQueue.isEmpty()) {
                PurchaseItem purchase = pQueue.get(0);
                PaymentItem payment = payQueue.get(0);
                double matched = Math.min(purchase.amount, payment.amount);
                int delayDays = (int) java.time.temporal.ChronoUnit.DAYS.between(purchase.date, payment.date);

                if (delayDays > DAYS_THRESHOLD) {
                    var computed = computeItcAndInterest(matched, delayDays);
                    results.add(InterestRow.builder()
                            .supplier(supplier)
                            .purchaseDate(purchase.date)
                            .paymentDate(payment.date)
                            .principal(matched)
                            .delayDays(delayDays)
                            .itcAmount(computed.itcAmount())
                            .interest(computed.interest())
                            .status(InterestRow.InterestStatus.PAID_LATE)
                            .build());
                }

                purchase.amount -= matched;
                payment.amount -= matched;
                if (purchase.amount <= 0) pQueue.remove(0);
                if (payment.amount <= 0) payQueue.remove(0);
            }

            // Unmatched purchases: UNPAID
            for (PurchaseItem purchase : pQueue) {
                int delayDays = (int) java.time.temporal.ChronoUnit.DAYS.between(purchase.date, asOnDate);
                if (delayDays > DAYS_THRESHOLD) {
                    var computed = computeItcAndInterest(purchase.amount, delayDays);
                    results.add(InterestRow.builder()
                            .supplier(supplier)
                            .purchaseDate(purchase.date)
                            .paymentDate(null)
                            .principal(purchase.amount)
                            .delayDays(delayDays)
                            .itcAmount(computed.itcAmount())
                            .interest(computed.interest())
                            .status(InterestRow.InterestStatus.UNPAID)
                            .build());
                }
            }
        }

        double totalInterest = results.stream().mapToDouble(InterestRow::getInterest).sum();
        double totalItcReversal = results.stream()
                .filter(r -> r.getStatus() == InterestRow.InterestStatus.UNPAID)
                .mapToDouble(InterestRow::getItcAmount)
                .sum();

        return CalculationSummary.builder()
                .totalInterest(round(totalInterest, 2))
                .totalItcReversal(round(totalItcReversal, 2))
                .details(results)
                .build();
    }

    private static ItcInterest computeItcAndInterest(double principal, int delayDays) {
        double itcAmount = round(principal * ITC_RATE, 2);
        double interest = round(itcAmount * INTEREST_RATE * delayDays / 365, 2);
        return new ItcInterest(itcAmount, interest);
    }

    private static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    private record ItcInterest(double itcAmount, double interest) {}

    private static class PurchaseItem {
        LocalDate date;
        double amount;

        PurchaseItem(LocalDate date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }

    private static class PaymentItem {
        LocalDate date;
        double amount;

        PaymentItem(LocalDate date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }
}
