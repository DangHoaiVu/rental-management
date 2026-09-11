package vn.hoaivu.rentalmanagement.application.property;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.property.persistence.PropertyJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.property.persistence.PropertyJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.room.persistence.RoomJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.room.persistence.RoomJpaRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyJpaRepository propertyRepository;
    private final RoomJpaRepository roomRepository;
    private final Clock clock = Clock.systemUTC();

    public PropertyService(PropertyJpaRepository propertyRepository, RoomJpaRepository roomRepository) {
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<PropertyView> listProperties(UUID ownerUserId) {
        return propertyRepository.findAllByOwnerUserIdOrderByName(ownerUserId).stream()
                .map(PropertyView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyView getProperty(UUID ownerUserId, UUID propertyId) {
        return PropertyView.from(requireProperty(ownerUserId, propertyId));
    }

    @Transactional
    public PropertyView createProperty(UUID ownerUserId, String name, String address) {
        var property = propertyRepository.save(new PropertyJpaEntity(
                UUID.randomUUID(), ownerUserId, name.trim(), address.trim(), "ACTIVE", clock.instant()));
        return PropertyView.from(property);
    }

    @Transactional
    public PropertyView updateProperty(UUID ownerUserId, UUID propertyId, String name, String address, String status) {
        var property = propertyRepository.findByIdAndOwnerUserId(propertyId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.update(name.trim(), address.trim(), status);
        return PropertyView.from(property);
    }

    @Transactional
    public void archiveProperty(UUID ownerUserId, UUID propertyId) {
        var property = requireProperty(ownerUserId, propertyId);
        property.update(property.getName(), property.getAddress(), "ARCHIVED");
    }

    @Transactional(readOnly = true)
    public List<RoomView> listRooms(UUID ownerUserId, UUID propertyId) {
        requireProperty(ownerUserId, propertyId);
        return roomRepository.findAllByPropertyIdOrderByCode(propertyId).stream()
                .map(RoomView::from)
                .toList();
    }

    @Transactional
    public RoomView createRoom(UUID ownerUserId, UUID propertyId, String code, int capacity) {
        var property = requireProperty(ownerUserId, propertyId);
        return RoomView.from(roomRepository.save(new RoomJpaEntity(
                UUID.randomUUID(), property, code.trim(), capacity, "ACTIVE")));
    }

    @Transactional
    public RoomView updateRoom(UUID ownerUserId, UUID propertyId, UUID roomId,
            String code, int capacity, String status) {
        requireProperty(ownerUserId, propertyId);
        var room = roomRepository.findByIdAndPropertyId(roomId, propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.update(code.trim(), capacity, status);
        return RoomView.from(room);
    }

    @Transactional
    public void archiveRoom(UUID ownerUserId, UUID propertyId, UUID roomId) {
        requireProperty(ownerUserId, propertyId);
        var room = roomRepository.findByIdAndPropertyId(roomId, propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.update(room.getCode(), room.getCapacity(), "ARCHIVED");
    }

    private PropertyJpaEntity requireProperty(UUID ownerUserId, UUID propertyId) {
        return propertyRepository.findByIdAndOwnerUserId(propertyId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
    }

    public record PropertyView(UUID id, String name, String address, String status) {
        static PropertyView from(PropertyJpaEntity property) {
            return new PropertyView(property.getId(), property.getName(), property.getAddress(), property.getStatus());
        }
    }

    public record RoomView(UUID id, UUID propertyId, String code, int capacity, String status) {
        static RoomView from(RoomJpaEntity room) {
            return new RoomView(room.getId(), room.getProperty().getId(), room.getCode(), room.getCapacity(), room.getStatus());
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}