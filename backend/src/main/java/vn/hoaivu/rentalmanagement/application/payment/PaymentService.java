package vn.hoaivu.rentalmanagement.application.payment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.payment.persistence.PaymentJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.payment.persistence.PaymentJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.room.persistence.RoomJpaRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final InvoiceJpaRepository invoiceRepository;
    private final PaymentJpaRepository paymentRepository;
    private final LeaseJpaRepository leaseRepository;
    private final RoomJpaRepository roomRepository;
    private final Clock clock = Clock.systemUTC();

    public PaymentService(InvoiceJpaRepository invoiceRepository, PaymentJpaRepository paymentRepository,
            LeaseJpaRepository leaseRepository, RoomJpaRepository roomRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.leaseRepository = leaseRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public PaymentResult record(UUID landlordUserId, UUID invoiceId, String idempotencyKey, BigDecimal amount) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new PaymentRuleException("Idempotency-Key is required");
        }
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new PaymentRuleException("Payment amount must be positive and use at most two decimals");
        }

        InvoiceJpaEntity invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new PaymentRuleException("Invoice not found"));
        requireOwnedInvoice(landlordUserId, invoice);
        String fingerprint = fingerprint(invoiceId, amount);

        var existing = paymentRepository.findByInvoiceIdAndIdempotencyKey(invoiceId, idempotencyKey);
        if (existing.isPresent()) {
            if (!existing.get().getRequestFingerprint().equals(fingerprint)) {
                throw new PaymentConflictException("Idempotency key was already used with different data");
            }
            return result(invoice, existing.get(), currentPaid(invoiceId));
        }
        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new PaymentRuleException("Payments require an ISSUED invoice");
        }

        BigDecimal paid = currentPaid(invoiceId);
        BigDecimal remaining = invoice.getTotalAmount().subtract(paid);
        if (amount.compareTo(remaining) > 0) {
            throw new PaymentRuleException("Payment exceeds the invoice balance");
        }

        try {
            PaymentJpaEntity payment = paymentRepository.save(new PaymentJpaEntity(
                    UUID.randomUUID(), invoiceId, idempotencyKey, fingerprint, amount, clock.instant(), landlordUserId));
            paymentRepository.flush();
            return result(invoice, payment, paid.add(amount));
        } catch (DataIntegrityViolationException exception) {
            throw new PaymentConflictException("Payment request conflicts with an existing idempotency key");
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentView> list(UUID landlordUserId, UUID invoiceId) {
        InvoiceJpaEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PaymentRuleException("Invoice not found"));
        requireOwnedInvoice(landlordUserId, invoice);
        return paymentRepository.findAllByInvoiceIdAndStatusOrderByReceivedAtAsc(invoiceId, "CONFIRMED")
                .stream().map(payment -> new PaymentView(payment.getId(), payment.getAmount(),
                        payment.getReceivedAt(), payment.getRecordedBy())).toList();
    }

    private BigDecimal currentPaid(UUID invoiceId) {
        return paymentRepository.findAllByInvoiceIdAndStatusOrderByReceivedAtAsc(invoiceId, "CONFIRMED")
                .stream().map(PaymentJpaEntity::getAmount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private PaymentResult result(InvoiceJpaEntity invoice, PaymentJpaEntity payment, BigDecimal paid) {
        String state = paid.compareTo(BigDecimal.ZERO) == 0 ? "UNPAID"
                : paid.compareTo(invoice.getTotalAmount()) >= 0 ? "PAID" : "PARTIALLY_PAID";
        return new PaymentResult(payment.getId(), invoice.getId(), payment.getAmount(), paid,
                invoice.getTotalAmount().subtract(paid), state);
    }

    private void requireOwnedInvoice(UUID landlordUserId, InvoiceJpaEntity invoice) {
        var lease = leaseRepository.findById(invoice.getLeaseId())
                .orElseThrow(() -> new PaymentRuleException("Lease not found"));
        if (roomRepository.findByIdAndPropertyOwnerUserId(lease.getRoomId(), landlordUserId).isEmpty()) {
            throw new PaymentRuleException("Invoice is not owned by this user");
        }
    }

    private String fingerprint(UUID invoiceId, BigDecimal amount) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((invoiceId + ":" + amount.setScale(2)).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record PaymentResult(UUID paymentId, UUID invoiceId, BigDecimal amount,
            BigDecimal paidAmount, BigDecimal remainingAmount, String invoiceState) {
    }

    public record PaymentView(UUID paymentId, BigDecimal amount, Instant receivedAt, UUID recordedBy) {
    }

    public static class PaymentRuleException extends RuntimeException {
        public PaymentRuleException(String message) { super(message); }
    }

    public static class PaymentConflictException extends RuntimeException {
        public PaymentConflictException(String message) { super(message); }
    }
}
