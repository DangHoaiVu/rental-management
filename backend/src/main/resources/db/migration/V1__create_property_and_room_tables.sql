-- owner_user_id intentionally has no FK until the identity schema is introduced.
-- A later identity migration must add users and then the FK without dropping data.
CREATE TABLE properties (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL CHECK (btrim(name) <> ''),
    address TEXT NOT NULL CHECK (btrim(address) <> ''),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL CHECK (btrim(code) <> ''),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT uq_rooms_property_code UNIQUE (property_id, code)
);