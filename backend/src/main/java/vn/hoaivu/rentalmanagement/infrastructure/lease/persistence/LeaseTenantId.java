package vn.hoaivu.rentalmanagement.infrastructure.lease.persistence;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class LeaseTenantId implements Serializable {
    private UUID leaseId;
    private UUID tenantId;

    protected LeaseTenantId() {
    }

    public LeaseTenantId(UUID leaseId, UUID tenantId) {
        this.leaseId = leaseId;
        this.tenantId = tenantId;
    }

    public UUID getLeaseId() { return leaseId; }
    public UUID getTenantId() { return tenantId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof LeaseTenantId that)) return false;
        return leaseId.equals(that.leaseId) && tenantId.equals(that.tenantId);
    }

    @Override
    public int hashCode() {
        return 31 * leaseId.hashCode() + tenantId.hashCode();
    }
}
