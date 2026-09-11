package vn.hoaivu.rentalmanagement.api.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoaivu.rentalmanagement.application.identity.AuthService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            var result = authService.login(request.email(), request.password());
            return ResponseEntity.ok(new LoginResponse(
                    result.token(), result.expiresAt(), result.user().getId(),
                    result.user().getEmail(), result.user().getRole()));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("invalid_credentials", "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization.substring("Bearer ".length()).trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        var user = (AuthService.AuthenticatedUser) authentication.getDetails();
        return new CurrentUserResponse(user.id(), user.email(), user.role());
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginResponse(
            String token,
            Instant expiresAt,
            UUID userId,
            String email,
            String role) {
    }

    public record CurrentUserResponse(UUID userId, String email, String role) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
