package com.learning.backendservice.service.export;

import com.learning.backendservice.domain.rule37.LedgerResult;

import java.util.List;

/**
 * Interface for exporting Rule 37 calculation results.
 * Phase 1: Excel. Future: PDF, JSON, CSV.
 */
public interface ExportStrategy {

    /**
     * Generates export bytes from ledger results.
     *
     * @param ledgerResults list of ledger results
     * @param filename      base filename for the export
     * @return export bytes
     */
    byte[] generate(List<LedgerResult> ledgerResults, String filename);

    /**
     * Returns the content type for the export (e.g. application/vnd.openxmlformats-officedocument.spreadsheetml.sheet).
     */
    String getContentType();

    /**
     * Returns the file extension (e.g. xlsx).
     */
    String getFileExtension();
}
