package vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class InvoiceJpaEntity {

    @Id
    private UUID id;
    @Column(name = "lease_id", nullable = false) private UUID leaseId;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private BigDecimal totalAmount;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "issued_by") private UUID issuedBy;
    @Column(name = "issued_at") private Instant issuedAt;

    protected InvoiceJpaEntity() {
    }

    public InvoiceJpaEntity(UUID id, UUID leaseId, LocalDate periodStart, LocalDate periodEnd,
            LocalDate dueDate) {
        this.id = id;
        this.leaseId = leaseId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
        this.totalAmount = BigDecimal.ZERO.setScale(2);
        this.status = "DRAFT";
    }

    public UUID getId() { return id; }
    public UUID getLeaseId() { return leaseId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }

    public void issue(BigDecimal total, UUID issuedBy, Instant issuedAt) {
        this.totalAmount = total;
        this.status = "ISSUED";
        this.issuedBy = issuedBy;
        this.issuedAt = issuedAt;
    }
}
