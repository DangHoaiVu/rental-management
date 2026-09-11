package vn.hoaivu.rentalmanagement.application.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.UserJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence.TenantJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence.TenantJpaRepository;

import java.time.Clock;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantJpaRepository tenantRepository;
    private final UserJpaRepository userRepository;
    private final Clock clock = Clock.systemUTC();

    public TenantService(TenantJpaRepository tenantRepository, UserJpaRepository userRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TenantView createTenant(UUID userId, String fullName, String phone) {
        if (userId != null && !userRepository.existsByIdAndRoleAndStatus(userId, "TENANT", "ACTIVE")) {
            throw new TenantRuleException("userId must reference an active TENANT user");
        }
        var tenant = tenantRepository.save(new TenantJpaEntity(
                UUID.randomUUID(), userId, fullName.trim(), phone == null ? null : phone.trim(), clock.instant()));
        return TenantView.from(tenant);
    }

    public record TenantView(UUID id, UUID userId, String fullName, String phone) {
        static TenantView from(TenantJpaEntity tenant) {
            return new TenantView(tenant.getId(), tenant.getUserId(), tenant.getFullName(), tenant.getPhone());
        }
    }

    public static class TenantRuleException extends RuntimeException {
        public TenantRuleException(String message) {
            super(message);
        }
    }
}
