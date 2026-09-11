package vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TenantJpaEntity() {
    }

    public TenantJpaEntity(UUID id, UUID userId, String fullName, String phone, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
}
