CREATE TABLE IF NOT EXISTS entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default',
    entry_key VARCHAR(255) NOT NULL,
    entry_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(255),
    UNIQUE(tenant_id, entry_key)
    );

-- Indexes for performance
CREATE INDEX idx_entries_tenant ON entries(tenant_id);
CREATE INDEX idx_entries_key ON entries(entry_key);
CREATE INDEX idx_entries_tenant_key ON entries(tenant_id, entry_key);
CREATE INDEX idx_entries_created_at ON entries(created_at DESC);
CREATE INDEX idx_entries_created_by ON entries(created_by);

-- Comments for documentation
COMMENT ON TABLE entries IS 'Key-value entries with row-level tenant isolation';
COMMENT ON COLUMN entries.tenant_id IS 'Tenant identifier for row-level isolation';
COMMENT ON COLUMN entries.entry_key IS 'Unique key within this tenant';
COMMENT ON COLUMN entries.entry_value IS 'Value associated with the key';
