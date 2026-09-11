package vn.hoaivu.rentalmanagement.application.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.billing.persistence.ChargeRateJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.billing.persistence.ChargeRateJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.property.persistence.PropertyJpaRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ChargeRateService {

    private final ChargeRateJpaRepository rateRepository;
    private final PropertyJpaRepository propertyRepository;
    private final Clock clock = Clock.systemUTC();

    public ChargeRateService(ChargeRateJpaRepository rateRepository, PropertyJpaRepository propertyRepository) {
        this.rateRepository = rateRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional(readOnly = true)
    public List<ChargeRateView> list(UUID ownerUserId, UUID propertyId) {
        requireOwnedProperty(ownerUserId, propertyId);
        return rateRepository.findAllByPropertyIdOrderByCodeAscEffectiveFromAsc(propertyId).stream()
                .map(ChargeRateView::from)
                .toList();
    }

    @Transactional
    public ChargeRateView create(UUID ownerUserId, UUID propertyId, String code, String name, String unit,
            BigDecimal unitPrice, LocalDate effectiveFrom, LocalDate effectiveTo) {
        requireOwnedProperty(ownerUserId, propertyId);
        validatePeriod(effectiveFrom, effectiveTo);
        var rate = rateRepository.save(new ChargeRateJpaEntity(UUID.randomUUID(), propertyId,
                code.trim(), name.trim(), unit.trim(), unitPrice, effectiveFrom, effectiveTo, clock.instant()));
        return ChargeRateView.from(rate);
    }

    @Transactional(readOnly = true)
    public ChargeRateView findForPeriod(UUID ownerUserId, UUID propertyId, String code, LocalDate periodStart) {
        requireOwnedProperty(ownerUserId, propertyId);
        if (!periodStart.equals(periodStart.withDayOfMonth(1))) {
            throw new RateRuleException("periodStart must be the first day of a month");
        }
        return rateRepository.findEffectiveRate(propertyId, code, periodStart)
                .map(ChargeRateView::from)
                .orElseThrow(() -> new RateRuleException("No charge rate is effective for this period"));
    }

    private void validatePeriod(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (!effectiveFrom.equals(effectiveFrom.withDayOfMonth(1))) {
            throw new RateRuleException("effectiveFrom must be the first day of a month");
        }
        if (effectiveTo != null && (!effectiveTo.equals(effectiveTo.withDayOfMonth(1))
                || !effectiveTo.isAfter(effectiveFrom))) {
            throw new RateRuleException("effectiveTo must be a later first day of a month");
        }
    }

    private void requireOwnedProperty(UUID ownerUserId, UUID propertyId) {
        if (propertyRepository.findByIdAndOwnerUserId(propertyId, ownerUserId).isEmpty()) {
            throw new RateRuleException("Property is not owned by this user");
        }
    }

    public record ChargeRateView(UUID id, UUID propertyId, String code, String name, String unit,
            BigDecimal unitPrice, LocalDate effectiveFrom, LocalDate effectiveTo) {
        static ChargeRateView from(ChargeRateJpaEntity rate) {
            return new ChargeRateView(rate.getId(), rate.getPropertyId(), rate.getCode(), rate.getName(),
                    rate.getUnit(), rate.getUnitPrice(), rate.getEffectiveFrom(), rate.getEffectiveTo());
        }
    }

    public static class RateRuleException extends RuntimeException {
        public RateRuleException(String message) {
            super(message);
        }
    }
}
