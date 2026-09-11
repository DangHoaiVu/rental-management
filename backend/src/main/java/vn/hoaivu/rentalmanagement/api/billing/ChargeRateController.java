package vn.hoaivu.rentalmanagement.api.billing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.billing.ChargeRateService;
import vn.hoaivu.rentalmanagement.application.identity.AuthService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/charge-rates")
@PreAuthorize("hasRole('LANDLORD')")
public class ChargeRateController {

    private final ChargeRateService rateService;

    public ChargeRateController(ChargeRateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping
    public List<ChargeRateService.ChargeRateView> list(Authentication authentication,
            @PathVariable UUID propertyId) {
        return rateService.list(userId(authentication), propertyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChargeRateService.ChargeRateView create(Authentication authentication,
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateRateRequest request) {
        return rateService.create(userId(authentication), propertyId, request.code(), request.name(), request.unit(),
                request.unitPrice(), request.effectiveFrom(), request.effectiveTo());
    }

    @GetMapping("/effective")
    public ChargeRateService.ChargeRateView findEffective(Authentication authentication,
            @PathVariable UUID propertyId,
            @RequestParam String code,
            @RequestParam LocalDate periodStart) {
        return rateService.findForPeriod(userId(authentication), propertyId, code, periodStart);
    }

    private UUID userId(Authentication authentication) {
        return ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
    }

    public record CreateRateRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 30) String unit,
            @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo) {
    }
}
