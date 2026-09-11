package vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.UUID;
import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<InvoiceJpaEntity> findByIdForUpdate(UUID id);
}
