package vn.hoaivu.rentalmanagement.infrastructure.lease.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaseJpaRepository extends JpaRepository<LeaseJpaEntity, UUID> {
    List<LeaseJpaEntity> findAllByRoomIdOrderByStartDate(UUID roomId);
}
