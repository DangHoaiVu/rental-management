package vn.hoaivu.rentalmanagement.infrastructure.meter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeterReadingJpaRepository extends JpaRepository<MeterReadingJpaEntity, UUID> {
    Optional<MeterReadingJpaEntity> findTopByLeaseIdAndMeterTypeOrderByReadingDateDesc(UUID leaseId, String meterType);
}
