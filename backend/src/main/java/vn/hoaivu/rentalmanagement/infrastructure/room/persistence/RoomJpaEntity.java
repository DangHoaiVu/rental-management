package vn.hoaivu.rentalmanagement.infrastructure.room.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import vn.hoaivu.rentalmanagement.infrastructure.property.persistence.PropertyJpaEntity;

import java.util.UUID;

@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(name = "uq_rooms_property_code", columnNames = {"property_id", "code"}))
public class RoomJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyJpaEntity property;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, length = 20)
    private String status;

    protected RoomJpaEntity() {
    }

    public RoomJpaEntity(UUID id, PropertyJpaEntity property, String code, int capacity, String status) {
        this.id = id;
        this.property = property;
        this.code = code;
        this.capacity = capacity;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public PropertyJpaEntity getProperty() {
        return property;
    }

    public String getCode() {
        return code;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public void update(String code, int capacity, String status) {
        this.code = code;
        this.capacity = capacity;
        this.status = status;
    }
}