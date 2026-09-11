package vn.hoaivu.rentalmanagement.api.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.identity.AuthService;
import vn.hoaivu.rentalmanagement.application.payment.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices/{invoiceId}/payments")
@PreAuthorize("hasRole('LANDLORD')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentService.PaymentResult record(Authentication authentication,
            @PathVariable UUID invoiceId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        UUID landlordId = ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
        return paymentService.record(landlordId, invoiceId, idempotencyKey, request.amount());
    }

    @GetMapping
    public List<PaymentService.PaymentView> list(Authentication authentication, @PathVariable UUID invoiceId) {
        UUID landlordId = ((AuthService.AuthenticatedUser) authentication.getDetails()).id();
        return paymentService.list(landlordId, invoiceId);
    }

    public record PaymentRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {
    }
}
