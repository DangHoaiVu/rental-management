package vn.hoaivu.rentalmanagement.infrastructure.identity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionJpaEntity, UUID> {

    Optional<AuthSessionJpaEntity> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash,
            Instant currentTime);

    Optional<AuthSessionJpaEntity> findByTokenHash(String tokenHash);
}
