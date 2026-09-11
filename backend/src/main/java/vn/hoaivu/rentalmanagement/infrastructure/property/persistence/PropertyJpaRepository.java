package vn.hoaivu.rentalmanagement.infrastructure.property.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, UUID> {

    List<PropertyJpaEntity> findAllByOwnerUserIdOrderByName(UUID ownerUserId);

    Optional<PropertyJpaEntity> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);
}