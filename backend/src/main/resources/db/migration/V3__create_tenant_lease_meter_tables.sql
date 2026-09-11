CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    full_name VARCHAR(160) NOT NULL CHECK (btrim(full_name) <> ''),
    phone VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leases (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE,
    rent_amount NUMERIC(19, 2) NOT NULL CHECK (rent_amount >= 0),
    deposit_amount NUMERIC(19, 2) NOT NULL CHECK (deposit_amount >= 0),
    handover_electricity NUMERIC(19, 3) NOT NULL CHECK (handover_electricity >= 0),
    handover_water NUMERIC(19, 3) NOT NULL CHECK (handover_water >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ENDED', 'CANCELLED')),
    version INTEGER NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_leases_date_order CHECK (end_date IS NULL OR end_date > start_date),
    CONSTRAINT ex_leases_room_period EXCLUDE USING gist (
        room_id WITH =,
        daterange(start_date, COALESCE(end_date, 'infinity'::date), '[)') WITH &&
    ) WHERE (status IN ('ACTIVE', 'ENDED'))
);

CREATE TABLE lease_tenants (
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    relationship VARCHAR(20) NOT NULL CHECK (relationship IN ('REPRESENTATIVE', 'OCCUPANT')),
    PRIMARY KEY (lease_id, tenant_id)
);

CREATE UNIQUE INDEX uq_lease_representative
    ON lease_tenants (lease_id)
    WHERE relationship = 'REPRESENTATIVE';

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY,
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    meter_type VARCHAR(11) NOT NULL CHECK (meter_type IN ('ELECTRICITY', 'WATER')),
    reading_date DATE NOT NULL,
    reading_value NUMERIC(19, 3) NOT NULL CHECK (reading_value >= 0),
    recorded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_meter_reading_date UNIQUE (lease_id, meter_type, reading_date)
);

CREATE INDEX ix_leases_room_id ON leases (room_id);
CREATE INDEX ix_lease_tenants_tenant_id ON lease_tenants (tenant_id);
CREATE INDEX ix_meter_readings_lease_type_date ON meter_readings (lease_id, meter_type, reading_date);
