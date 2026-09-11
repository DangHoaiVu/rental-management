package vn.hoaivu.rentalmanagement.infrastructure.payment.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    Optional<PaymentJpaEntity> findByInvoiceIdAndIdempotencyKey(UUID invoiceId, String idempotencyKey);
    List<PaymentJpaEntity> findAllByInvoiceIdAndStatusOrderByReceivedAtAsc(UUID invoiceId, String status);
}
