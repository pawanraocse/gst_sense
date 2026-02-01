/**
 * Rule 37 (180-day ITC reversal) models.
 * Maps to backend DTOs.
 */

export interface InterestRow {
  supplier: string;
  purchaseDate: string;
  paymentDate: string | null;
  principal: number;
  delayDays: number;
  itcAmount: number;
  interest: number;
  status: 'PAID_LATE' | 'UNPAID';
}

export interface CalculationSummary {
  totalInterest: number;
  totalItcReversal: number;
  details: InterestRow[];
}

export interface LedgerResult {
  ledgerName: string;
  summary: CalculationSummary;
}

export interface UploadResult {
  runId: number;
  filename: string;
  results: { ledgerName: string; summary: CalculationSummary }[];
  errors: { filename: string; message: string }[];
}

export interface Rule37RunResponse {
  id: number;
  filename: string;
  asOnDate: string;
  totalInterest: number | null;
  totalItcReversal: number | null;
  createdAt: string;
  createdBy: string | null;
  expiresAt: string;
  calculationData: LedgerResult[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
