package vn.hoaivu.rentalmanagement.infrastructure.identity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByIdAndRoleAndStatus(UUID id, String role, String status);
}
