package vn.hoaivu.rentalmanagement.api.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.identity.AuthService;
import vn.hoaivu.rentalmanagement.application.property.PropertyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@PreAuthorize("hasRole('LANDLORD')")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<PropertyService.PropertyView> list(Authentication authentication) {
        return propertyService.listProperties(userId(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyService.PropertyView create(
            Authentication authentication,
            @Valid @RequestBody CreatePropertyRequest request) {
        return propertyService.createProperty(userId(authentication), request.name(), request.address());
    }

    @GetMapping("/{propertyId}")
    public PropertyService.PropertyView get(Authentication authentication, @PathVariable UUID propertyId) {
        return propertyService.getProperty(userId(authentication), propertyId);
    }

    @PutMapping("/{propertyId}")
    public PropertyService.PropertyView update(
            Authentication authentication,
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdatePropertyRequest request) {
        return propertyService.updateProperty(
                userId(authentication), propertyId, request.name(), request.address(), request.status());
    }

    @DeleteMapping("/{propertyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(Authentication authentication, @PathVariable UUID propertyId) {
        propertyService.archiveProperty(userId(authentication), propertyId);
    }

    @GetMapping("/{propertyId}/rooms")
    public List<PropertyService.RoomView> listRooms(Authentication authentication, @PathVariable UUID propertyId) {
        return propertyService.listRooms(userId(authentication), propertyId);
    }

    @PostMapping("/{propertyId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyService.RoomView createRoom(
            Authentication authentication,
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateRoomRequest request) {
        return propertyService.createRoom(userId(authentication), propertyId, request.code(), request.capacity());
    }

    @PutMapping("/{propertyId}/rooms/{roomId}")
    public PropertyService.RoomView updateRoom(
            Authentication authentication,
            @PathVariable UUID propertyId,
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        return propertyService.updateRoom(
                userId(authentication), propertyId, roomId, request.code(), request.capacity(), request.status());
    }

    @DeleteMapping("/{propertyId}/rooms/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveRoom(Authentication authentication, @PathVariable UUID propertyId, @PathVariable UUID roomId) {
        propertyService.archiveRoom(userId(authentication), propertyId, roomId);
    }

    private UUID userId(Authentication authentication) {
        return ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
    }

        public record CreatePropertyRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank String address) {
        }

        public record UpdatePropertyRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank String address,
            @NotBlank @Size(max = 20) String status) {
    }

        public record CreateRoomRequest(
            @NotBlank @Size(max = 50) String code,
            @Min(1) int capacity) {
        }

        public record UpdateRoomRequest(
            @NotBlank @Size(max = 50) String code,
            @Min(1) int capacity,
            @NotBlank @Size(max = 20) String status) {
    }
}
