package vn.hoaivu.rentalmanagement.infrastructure.payment.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    private UUID id;
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    @Column(name = "idempotency_key", nullable = false, length = 100) private String idempotencyKey;
    @Column(name = "request_fingerprint", nullable = false, length = 128) private String requestFingerprint;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;
    @Column(name = "recorded_by", nullable = false) private UUID recordedBy;
    @Column(nullable = false, length = 20) private String status;

    protected PaymentJpaEntity() {
    }

    public PaymentJpaEntity(UUID id, UUID invoiceId, String idempotencyKey, String requestFingerprint,
            BigDecimal amount, Instant receivedAt, UUID recordedBy) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.amount = amount;
        this.receivedAt = receivedAt;
        this.recordedBy = recordedBy;
        this.status = "CONFIRMED";
    }

    public UUID getId() { return id; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestFingerprint() { return requestFingerprint; }
    public BigDecimal getAmount() { return amount; }
    public Instant getReceivedAt() { return receivedAt; }
    public UUID getRecordedBy() { return recordedBy; }
    public String getStatus() { return status; }
}
