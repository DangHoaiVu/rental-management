package vn.hoaivu.rentalmanagement.api.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.tenant.TenantService;

@RestController
@RequestMapping("/api/v1/tenants")
@PreAuthorize("hasRole('LANDLORD')")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantService.TenantView create(
            @Valid @RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request.userId(), request.fullName(), request.phone());
    }

    public record CreateTenantRequest(
            java.util.UUID userId,
            @NotBlank @Size(max = 160) String fullName,
            @Size(max = 30) String phone) {
    }
}
