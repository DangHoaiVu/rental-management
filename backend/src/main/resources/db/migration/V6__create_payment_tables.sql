CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(100) NOT NULL,
    request_fingerprint VARCHAR(128) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    received_at TIMESTAMPTZ NOT NULL,
    recorded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
        CHECK (status IN ('CONFIRMED')),
    CONSTRAINT uq_payment_invoice_idempotency UNIQUE (invoice_id, idempotency_key)
);

CREATE INDEX ix_payments_invoice_status ON payments (invoice_id, status);
