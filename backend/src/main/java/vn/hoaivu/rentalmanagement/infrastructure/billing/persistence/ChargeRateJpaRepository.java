package vn.hoaivu.rentalmanagement.infrastructure.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRateJpaRepository extends JpaRepository<ChargeRateJpaEntity, UUID> {

    List<ChargeRateJpaEntity> findAllByPropertyIdOrderByCodeAscEffectiveFromAsc(UUID propertyId);

    @Query("""
            select rate from ChargeRateJpaEntity rate
            where rate.propertyId = :propertyId
              and rate.code = :code
              and rate.effectiveFrom <= :periodStart
              and (rate.effectiveTo is null or :periodStart < rate.effectiveTo)
            """)
    Optional<ChargeRateJpaEntity> findEffectiveRate(
            @Param("propertyId") UUID propertyId,
            @Param("code") String code,
            @Param("periodStart") LocalDate periodStart);
}
