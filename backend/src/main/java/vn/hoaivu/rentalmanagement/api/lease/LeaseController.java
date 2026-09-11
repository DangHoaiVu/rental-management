package vn.hoaivu.rentalmanagement.api.lease;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.identity.AuthService;
import vn.hoaivu.rentalmanagement.application.lease.LeaseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LeaseController {

    private final LeaseService leaseService;

    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    @PostMapping("/rooms/{roomId}/leases")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LANDLORD')")
    public LeaseService.LeaseView create(
            Authentication authentication,
            @PathVariable UUID roomId,
            @Valid @RequestBody CreateLeaseRequest request) {
        return leaseService.createLease(userId(authentication), roomId, request.startDate(), request.endDate(),
                request.rentAmount(), request.depositAmount(), request.handoverElectricity(), request.handoverWater());
    }

    @GetMapping("/leases/{leaseId}")
    public LeaseService.LeaseView get(Authentication authentication, @PathVariable UUID leaseId) {
        var user = (AuthService.AuthenticatedUser) authentication.getDetails();
        return leaseService.getLease(user.id(), user.role(), leaseId);
    }

    @PostMapping("/leases/{leaseId}/tenants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('LANDLORD')")
    public void addTenant(Authentication authentication,
            @PathVariable UUID leaseId, @Valid @RequestBody AddTenantRequest request) {
        leaseService.addTenant(userId(authentication), leaseId, request.tenantId(), request.relationship());
    }

    @PostMapping("/leases/{leaseId}/activate")
    @PreAuthorize("hasRole('LANDLORD')")
    public LeaseService.LeaseView activate(Authentication authentication, @PathVariable UUID leaseId) {
        return leaseService.activate(userId(authentication), leaseId);
    }

    @PostMapping("/leases/{leaseId}/meter-readings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LANDLORD')")
    public LeaseService.MeterView addReading(Authentication authentication,
            @PathVariable UUID leaseId, @Valid @RequestBody MeterReadingRequest request) {
        return leaseService.addReading(userId(authentication), leaseId, request.meterType(),
                request.readingDate(), request.readingValue());
    }

    private UUID userId(Authentication authentication) {
        return ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
    }

    public record CreateLeaseRequest(
            @NotNull LocalDate startDate,
            LocalDate endDate,
            @NotNull @DecimalMin("0.00") BigDecimal rentAmount,
            @NotNull @DecimalMin("0.00") BigDecimal depositAmount,
            @NotNull @DecimalMin("0.000") BigDecimal handoverElectricity,
            @NotNull @DecimalMin("0.000") BigDecimal handoverWater) {
    }

    public record AddTenantRequest(
            @NotNull UUID tenantId,
            @NotNull @Pattern(regexp = "REPRESENTATIVE|OCCUPANT") String relationship) {
    }

    public record MeterReadingRequest(
            @NotNull @Pattern(regexp = "ELECTRICITY|WATER") String meterType,
            @NotNull LocalDate readingDate,
            @NotNull @DecimalMin("0.000") BigDecimal readingValue) {
    }
}
