package vn.hoaivu.rentalmanagement.application.identity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.AuthSessionJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.AuthSessionJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.UserJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.UserJpaRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Duration SESSION_LIFETIME = Duration.ofHours(8);

    private final UserJpaRepository userRepository;
    private final AuthSessionJpaRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final Clock clock;

    public AuthService(
            UserJpaRepository userRepository,
            AuthSessionJpaRepository sessionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public LoginResult login(String email, String password) {
        UserJpaEntity user = userRepository.findByEmail(email.trim().toLowerCase())
                .filter(candidate -> "ACTIVE".equals(candidate.getStatus()))
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        Instant now = clock.instant();
        Instant expiresAt = now.plus(SESSION_LIFETIME);
        String rawToken = createToken();
        sessionRepository.save(new AuthSessionJpaEntity(
                UUID.randomUUID(), user, hashToken(rawToken), expiresAt, now));
        return new LoginResult(rawToken, expiresAt, user);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedUser> authenticate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        hashToken(rawToken), clock.instant())
                .filter(session -> "ACTIVE".equals(session.getUser().getStatus()))
                .map(session -> new AuthenticatedUser(
                        session.getUser().getId(), session.getUser().getEmail(), session.getUser().getRole()));
    }

    @Transactional
    public void logout(String rawToken) {
        sessionRepository.findByTokenHash(hashToken(rawToken))
                .ifPresent(session -> session.revoke(clock.instant()));
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record LoginResult(String token, Instant expiresAt, UserJpaEntity user) {
    }

    public record AuthenticatedUser(UUID id, String email, String role) {
    }
}
