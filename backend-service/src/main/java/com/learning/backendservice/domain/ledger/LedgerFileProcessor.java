package com.learning.backendservice.domain.ledger;

import com.learning.backendservice.domain.rule37.LedgerResult;

import java.io.InputStream;
import java.time.LocalDate;

/**
 * Interface for processing one ledger file: parse → calculate → LedgerResult.
 * OOM-safe: processes one file at a time.
 *
 * @see com.learning.backendservice.domain.ledger.Rule37LedgerFileProcessor
 */
public interface LedgerFileProcessor {

    /**
     * Processes a single ledger file.
     *
     * @param inputStream the raw file content
     * @param filename    original filename (used for ledger name)
     * @param asOnDate    calculation date for Rule 37
     * @return ledger result; never null
     */
    LedgerResult process(InputStream inputStream, String filename, LocalDate asOnDate);
}
