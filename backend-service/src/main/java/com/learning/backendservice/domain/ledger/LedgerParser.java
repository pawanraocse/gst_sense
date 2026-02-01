package com.learning.backendservice.domain.ledger;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for parsing ledger data from various formats.
 * Phase 1: Excel (Tally/Busy). Future: PDF, CSV.
 *
 * @see LedgerExcelParser
 */
public interface LedgerParser {

    /**
     * Parses ledger entries from the given input stream.
     *
     * @param inputStream the raw file content
     * @param filename    original filename (used for supplier fallback when missing)
     * @return list of ledger entries; never null
     * @throws com.learning.backendservice.exception.LedgerParseException if parsing fails
     */
    List<LedgerEntry> parse(InputStream inputStream, String filename);
}
