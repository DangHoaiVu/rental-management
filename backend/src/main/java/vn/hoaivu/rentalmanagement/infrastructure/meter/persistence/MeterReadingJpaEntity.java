package vn.hoaivu.rentalmanagement.infrastructure.meter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "meter_readings")
public class MeterReadingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "lease_id", nullable = false)
    private UUID leaseId;

    @Column(name = "meter_type", nullable = false, length = 11)
    private String meterType;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "reading_value", nullable = false, precision = 19, scale = 3)
    private BigDecimal readingValue;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected MeterReadingJpaEntity() {
    }

    public MeterReadingJpaEntity(UUID id, UUID leaseId, String meterType, LocalDate readingDate,
            BigDecimal readingValue, UUID recordedBy, Instant recordedAt) {
        this.id = id;
        this.leaseId = leaseId;
        this.meterType = meterType;
        this.readingDate = readingDate;
        this.readingValue = readingValue;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt;
    }

    public LocalDate getReadingDate() { return readingDate; }
    public BigDecimal getReadingValue() { return readingValue; }
    public UUID getLeaseId() { return leaseId; }
    public String getMeterType() { return meterType; }
}
