package vn.hoaivu.rentalmanagement.infrastructure.room.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, UUID> {

    List<RoomJpaEntity> findAllByPropertyIdOrderByCode(UUID propertyId);

    Optional<RoomJpaEntity> findByIdAndPropertyId(UUID id, UUID propertyId);

    Optional<RoomJpaEntity> findByIdAndPropertyOwnerUserId(UUID id, UUID ownerUserId);
}