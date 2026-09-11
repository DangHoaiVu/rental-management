package vn.hoaivu.rentalmanagement.infrastructure.lease.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leases")
public class LeaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rent_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "deposit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "handover_electricity", nullable = false, precision = 19, scale = 3)
    private BigDecimal handoverElectricity;

    @Column(name = "handover_water", nullable = false, precision = 19, scale = 3)
    private BigDecimal handoverWater;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LeaseJpaEntity() {
    }

    public LeaseJpaEntity(UUID id, UUID roomId, LocalDate startDate, LocalDate endDate,
            BigDecimal rentAmount, BigDecimal depositAmount, BigDecimal handoverElectricity,
            BigDecimal handoverWater, String status, Instant now) {
        this.id = id;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.handoverElectricity = handoverElectricity;
        this.handoverWater = handoverWater;
        this.status = status;
        this.version = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getRoomId() { return roomId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public BigDecimal getHandoverElectricity() { return handoverElectricity; }
    public BigDecimal getHandoverWater() { return handoverWater; }
    public String getStatus() { return status; }

    public void activate(Instant now) {
        this.status = "ACTIVE";
        this.updatedAt = now;
        this.version++;
    }
}
