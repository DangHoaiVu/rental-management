package vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {
    List<TenantJpaEntity> findAllByOrderByFullName();

    Optional<TenantJpaEntity> findByUserId(UUID userId);
}
