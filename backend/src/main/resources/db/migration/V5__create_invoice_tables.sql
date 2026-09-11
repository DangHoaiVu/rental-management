CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'ISSUED')),
    issued_by UUID REFERENCES users(id) ON DELETE RESTRICT,
    issued_at TIMESTAMPTZ,
    CONSTRAINT uq_invoice_lease_period UNIQUE (lease_id, period_start),
    CONSTRAINT ck_invoice_period_order CHECK (period_end > period_start),
    CONSTRAINT ck_invoice_issued_metadata CHECK (
        (status = 'DRAFT' AND issued_by IS NULL AND issued_at IS NULL)
        OR (status = 'ISSUED' AND issued_by IS NOT NULL AND issued_at IS NOT NULL)
    )
);

CREATE TABLE invoice_lines (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    line_no INTEGER NOT NULL CHECK (line_no > 0),
    meter_type VARCHAR(11) CHECK (meter_type IS NULL OR meter_type IN ('ELECTRICITY', 'WATER')),
    meter_start_reading_id UUID REFERENCES meter_readings(id) ON DELETE RESTRICT,
    meter_end_reading_id UUID REFERENCES meter_readings(id) ON DELETE RESTRICT,
    source_rate_id UUID REFERENCES charge_rates(id) ON DELETE RESTRICT,
    charge_code VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    quantity NUMERIC(19, 3) NOT NULL CHECK (quantity >= 0),
    unit_price NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
    unit VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL DEFAULT 0 CHECK (amount >= 0),
    CONSTRAINT uq_invoice_line_number UNIQUE (invoice_id, line_no),
    CONSTRAINT ck_invoice_line_meter_pair CHECK (
        (meter_start_reading_id IS NULL AND meter_end_reading_id IS NULL)
        OR (meter_start_reading_id IS NOT NULL AND meter_end_reading_id IS NOT NULL)
    )
);

CREATE INDEX ix_invoices_lease_period ON invoices (lease_id, period_start);
CREATE INDEX ix_invoice_lines_invoice_id ON invoice_lines (invoice_id, line_no);
