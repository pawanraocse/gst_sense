/*
  # Phase 1: Rule 37 — GST Calculation Runs
  # Initial Schema (Consolidated)

  ## Summary
  - CREATE rule37_calculation_runs for Rule 37 ledger calculation results
  - Tenant ID for row-level isolation

  ## Retention
  - Default: 7 days (configurable via app.retention.days)
  - expires_at = created_at + retention; RetentionScheduler deletes expired runs
*/

CREATE TABLE rule37_calculation_runs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    as_on_date DATE NOT NULL,
    total_interest DECIMAL(15,2),
    total_itc_reversal DECIMAL(15,2),
    calculation_data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '7 days')
);

CREATE INDEX idx_rule37_runs_tenant ON rule37_calculation_runs(tenant_id, created_at DESC);
CREATE INDEX idx_rule37_runs_expires ON rule37_calculation_runs(expires_at);

COMMENT ON TABLE rule37_calculation_runs IS 'Rule 37 (180-day ITC reversal) calculation runs; ledger-based ingestion';
COMMENT ON COLUMN rule37_calculation_runs.tenant_id IS 'Tenant identifier for row-level isolation';
COMMENT ON COLUMN rule37_calculation_runs.calculation_data IS 'LedgerResult[] JSON; totals and InterestRow details';
COMMENT ON COLUMN rule37_calculation_runs.expires_at IS 'Retention expiry; default 7 days (configurable)';
