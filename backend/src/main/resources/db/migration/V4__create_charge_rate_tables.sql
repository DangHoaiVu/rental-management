CREATE TABLE charge_rates (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL CHECK (btrim(code) <> ''),
    name VARCHAR(160) NOT NULL CHECK (btrim(name) <> ''),
    unit VARCHAR(30) NOT NULL CHECK (btrim(unit) <> ''),
    unit_price NUMERIC(19, 2) NOT NULL CHECK (unit_price >= 0),
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_charge_rates_date_order CHECK (effective_to IS NULL OR effective_to > effective_from),
    CONSTRAINT ex_charge_rates_scope_period EXCLUDE USING gist (
        property_id WITH =,
        code WITH =,
        daterange(effective_from, COALESCE(effective_to, 'infinity'::date), '[)') WITH &&
    )
);

CREATE INDEX ix_charge_rates_property_code_period
    ON charge_rates (property_id, code, effective_from);
