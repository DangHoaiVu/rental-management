package vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.UUID;
import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT i FROM InvoiceJpaEntity i WHERE i.id = ?1")
	Optional<InvoiceJpaEntity> findByIdForUpdate(UUID id);
}
