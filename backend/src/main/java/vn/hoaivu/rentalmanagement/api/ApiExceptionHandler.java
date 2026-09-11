package vn.hoaivu.rentalmanagement.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.hoaivu.rentalmanagement.application.property.PropertyService.ResourceNotFoundException;
import vn.hoaivu.rentalmanagement.application.lease.LeaseService.LeaseRuleException;
import vn.hoaivu.rentalmanagement.application.tenant.TenantService.TenantRuleException;
import vn.hoaivu.rentalmanagement.application.billing.ChargeRateService.RateRuleException;
import vn.hoaivu.rentalmanagement.application.invoice.InvoiceService.InvoiceRuleException;
import vn.hoaivu.rentalmanagement.application.payment.PaymentService.PaymentConflictException;
import vn.hoaivu.rentalmanagement.application.payment.PaymentService.PaymentRuleException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validationError(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", "validation_error", "message", "Request validation failed"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", "not_found", "message", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("code", "conflict", "message", "Resource conflicts with existing data"));
    }

        @ExceptionHandler({LeaseRuleException.class, TenantRuleException.class, RateRuleException.class,
            InvoiceRuleException.class})
    ResponseEntity<Map<String, String>> businessRule(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("code", "business_rule_violation", "message", exception.getMessage()));
    }

    @ExceptionHandler(PaymentConflictException.class)
    ResponseEntity<Map<String, String>> paymentConflict(PaymentConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("code", "idempotency_conflict", "message", exception.getMessage()));
    }

    @ExceptionHandler(PaymentRuleException.class)
    ResponseEntity<Map<String, String>> paymentRule(PaymentRuleException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("code", "payment_rule_violation", "message", exception.getMessage()));
    }
}
