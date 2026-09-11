package vn.hoaivu.rentalmanagement.infrastructure.lease.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaseTenantJpaRepository extends JpaRepository<LeaseTenantJpaEntity, LeaseTenantId> {
    List<LeaseTenantJpaEntity> findAllByIdLeaseId(UUID leaseId);
    long countByIdLeaseIdAndRelationship(UUID leaseId, String relationship);
    List<LeaseTenantJpaEntity> findAllByIdTenantId(UUID tenantId);
}
