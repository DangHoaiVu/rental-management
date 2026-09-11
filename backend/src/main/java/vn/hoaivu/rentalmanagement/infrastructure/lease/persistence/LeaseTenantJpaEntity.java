package vn.hoaivu.rentalmanagement.infrastructure.lease.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "lease_tenants")
public class LeaseTenantJpaEntity {

    @EmbeddedId
    private LeaseTenantId id;

    @Column(nullable = false, length = 20)
    private String relationship;

    protected LeaseTenantJpaEntity() {
    }

    public LeaseTenantJpaEntity(LeaseTenantId id, String relationship) {
        this.id = id;
        this.relationship = relationship;
    }

    public LeaseTenantId getId() { return id; }
    public String getRelationship() { return relationship; }
}
