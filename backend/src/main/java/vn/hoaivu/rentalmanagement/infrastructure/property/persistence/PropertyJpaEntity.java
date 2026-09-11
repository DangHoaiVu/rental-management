package vn.hoaivu.rentalmanagement.infrastructure.property.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "properties")
public class PropertyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PropertyJpaEntity() {
    }

    public PropertyJpaEntity(UUID id, UUID ownerUserId, String name, String address, String status, Instant createdAt) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public void update(String name, String address, String status) {
        this.name = name;
        this.address = address;
        this.status = status;
    }
}