package vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_lines")
public class InvoiceLineJpaEntity {

    @Id private UUID id;
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    @Column(name = "line_no", nullable = false) private int lineNo;
    @Column(name = "meter_type", length = 11) private String meterType;
    @Column(name = "meter_start_reading_id") private UUID meterStartReadingId;
    @Column(name = "meter_end_reading_id") private UUID meterEndReadingId;
    @Column(name = "source_rate_id") private UUID sourceRateId;
    @Column(name = "charge_code", nullable = false, length = 50) private String chargeCode;
    @Column(nullable = false) private String description;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false, length = 30) private String unit;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;

    protected InvoiceLineJpaEntity() {
    }

    public InvoiceLineJpaEntity(UUID id, UUID invoiceId, int lineNo, String meterType,
            UUID meterStartReadingId, UUID meterEndReadingId, UUID sourceRateId,
            String chargeCode, String description, BigDecimal quantity, BigDecimal unitPrice,
            String unit, BigDecimal amount) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.lineNo = lineNo;
        this.meterType = meterType;
        this.meterStartReadingId = meterStartReadingId;
        this.meterEndReadingId = meterEndReadingId;
        this.sourceRateId = sourceRateId;
        this.chargeCode = chargeCode;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.amount = amount;
    }

    public void snapshot(String description, BigDecimal quantity, BigDecimal unitPrice,
            String unit, UUID sourceRateId, BigDecimal amount) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.sourceRateId = sourceRateId;
        this.amount = amount;
    }

    public UUID getId() { return id; }
    public UUID getInvoiceId() { return invoiceId; }
    public int getLineNo() { return lineNo; }
    public String getMeterType() { return meterType; }
    public UUID getMeterStartReadingId() { return meterStartReadingId; }
    public UUID getMeterEndReadingId() { return meterEndReadingId; }
    public String getChargeCode() { return chargeCode; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getUnit() { return unit; }
}
