package vn.hoaivu.rentalmanagement.api.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.identity.AuthService;
import vn.hoaivu.rentalmanagement.application.invoice.InvoiceService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('LANDLORD')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/leases/{leaseId}/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceService.InvoiceView createDraft(Authentication authentication,
            @PathVariable UUID leaseId,
            @Valid @RequestBody CreateDraftRequest request) {
        return invoiceService.createDraft(userId(authentication), leaseId, request.periodStart(), request.dueDate(),
                request.lines().stream().map(line -> new InvoiceService.LineRequest(line.meterType(),
                        line.meterStartReadingId(), line.meterEndReadingId(), line.chargeCode(),
                        line.description(), line.quantity())).toList());
    }

    @PostMapping("/invoices/{invoiceId}/issue")
    public InvoiceService.InvoiceView issue(Authentication authentication, @PathVariable UUID invoiceId) {
        return invoiceService.issue(userId(authentication), invoiceId);
    }

    private UUID userId(Authentication authentication) {
        return ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
    }

    public record CreateDraftRequest(
            @NotNull LocalDate periodStart,
            @NotNull LocalDate dueDate,
            List<@Valid DraftLineRequest> lines) {
        public CreateDraftRequest {
            if (lines == null) {
                lines = List.of();
            }
        }
    }

    public record DraftLineRequest(
            String meterType,
            UUID meterStartReadingId,
            UUID meterEndReadingId,
            @NotBlank @Size(max = 50) String chargeCode,
            @NotBlank String description,
            @NotNull @DecimalMin("0.000") BigDecimal quantity) {
    }
}
