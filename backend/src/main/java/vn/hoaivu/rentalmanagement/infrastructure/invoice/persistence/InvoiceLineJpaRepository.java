package vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceLineJpaRepository extends JpaRepository<InvoiceLineJpaEntity, UUID> {
    List<InvoiceLineJpaEntity> findAllByInvoiceIdOrderByLineNo(UUID invoiceId);
}
