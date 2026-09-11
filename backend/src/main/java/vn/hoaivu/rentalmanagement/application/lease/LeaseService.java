package vn.hoaivu.rentalmanagement.application.lease;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.identity.persistence.UserJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseTenantId;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseTenantJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseTenantJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.meter.persistence.MeterReadingJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.meter.persistence.MeterReadingJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.room.persistence.RoomJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence.TenantJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.tenant.persistence.TenantJpaRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LeaseService {

    private final LeaseJpaRepository leaseRepository;
    private final LeaseTenantJpaRepository leaseTenantRepository;
    private final TenantJpaRepository tenantRepository;
    private final RoomJpaRepository roomRepository;
    private final UserJpaRepository userRepository;
    private final MeterReadingJpaRepository readingRepository;
    private final Clock clock = Clock.systemUTC();

    public LeaseService(LeaseJpaRepository leaseRepository,
            LeaseTenantJpaRepository leaseTenantRepository,
            TenantJpaRepository tenantRepository,
            RoomJpaRepository roomRepository,
            UserJpaRepository userRepository,
            MeterReadingJpaRepository readingRepository) {
        this.leaseRepository = leaseRepository;
        this.leaseTenantRepository = leaseTenantRepository;
        this.tenantRepository = tenantRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.readingRepository = readingRepository;
    }

    @Transactional
    public LeaseView createLease(UUID ownerUserId, UUID roomId, LocalDate startDate, LocalDate endDate,
            BigDecimal rentAmount, BigDecimal depositAmount, BigDecimal handoverElectricity,
            BigDecimal handoverWater) {
        requireOwnedRoom(ownerUserId, roomId);
        validateDateRange(startDate, endDate);
        var lease = leaseRepository.save(new LeaseJpaEntity(UUID.randomUUID(), roomId, startDate, endDate,
                rentAmount, depositAmount, handoverElectricity, handoverWater, "DRAFT", clock.instant()));
        return LeaseView.from(lease);
    }

    @Transactional
    public void addTenant(UUID ownerUserId, UUID leaseId, UUID tenantId, String relationship) {
        var lease = requireOwnedLease(ownerUserId, leaseId);
        TenantJpaEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new LeaseRuleException("Tenant not found"));
        if ("REPRESENTATIVE".equals(relationship)
                && (tenant.getUserId() == null
                || !userRepository.existsByIdAndRoleAndStatus(tenant.getUserId(), "TENANT", "ACTIVE"))) {
            throw new LeaseRuleException("Representative must have an active TENANT account");
        }
        if (!"DRAFT".equals(lease.getStatus())) {
            throw new LeaseRuleException("Tenants can only be changed while lease is DRAFT");
        }
        leaseTenantRepository.save(new LeaseTenantJpaEntity(
                new LeaseTenantId(leaseId, tenantId), relationship));
    }

    @Transactional
    public LeaseView activate(UUID ownerUserId, UUID leaseId) {
        var lease = requireOwnedLease(ownerUserId, leaseId);
        if (!"DRAFT".equals(lease.getStatus())) {
            throw new LeaseRuleException("Only DRAFT lease can be activated");
        }
        var representatives = leaseTenantRepository.findAllByIdLeaseId(leaseId).stream()
                .filter(link -> "REPRESENTATIVE".equals(link.getRelationship()))
                .toList();
        if (representatives.size() != 1) {
            throw new LeaseRuleException("An ACTIVE lease must have exactly one representative");
        }
        var representative = tenantRepository.findById(representatives.get(0).getId().getTenantId())
                .orElseThrow(() -> new LeaseRuleException("Representative tenant not found"));
        if (representative.getUserId() == null
                || !userRepository.existsByIdAndRoleAndStatus(representative.getUserId(), "TENANT", "ACTIVE")) {
            throw new LeaseRuleException("Representative must have an active TENANT account");
        }
        try {
            lease.activate(clock.instant());
            leaseRepository.flush();
            return LeaseView.from(lease);
        } catch (DataIntegrityViolationException exception) {
            throw new LeaseRuleException("Lease overlaps another active lease for this room");
        }
    }

    @Transactional
    public MeterView addReading(UUID actorUserId, UUID leaseId, String meterType,
            LocalDate readingDate, BigDecimal readingValue) {
        var lease = requireOwnedLease(actorUserId, leaseId);
        if (!"ACTIVE".equals(lease.getStatus())) {
            throw new LeaseRuleException("Meter readings require an ACTIVE lease");
        }
        if (readingDate.isBefore(lease.getStartDate())
                || (lease.getEndDate() != null && !readingDate.isBefore(lease.getEndDate()))) {
            throw new LeaseRuleException("Reading date must be within the lease period");
        }
        var previous = readingRepository.findTopByLeaseIdAndMeterTypeOrderByReadingDateDesc(leaseId, meterType);
        if (previous.isPresent()) {
            if (!readingDate.isAfter(previous.get().getReadingDate())) {
                throw new LeaseRuleException("Reading date must be after the previous reading");
            }
            if (readingValue.compareTo(previous.get().getReadingValue()) < 0) {
                throw new LeaseRuleException("Reading value cannot decrease");
            }
        }
        var reading = readingRepository.save(new MeterReadingJpaEntity(
                UUID.randomUUID(), leaseId, meterType, readingDate, readingValue, actorUserId, clock.instant()));
        return new MeterView(readingDate, readingValue, meterType);
    }

    @Transactional(readOnly = true)
    public LeaseView getLease(UUID actorUserId, String actorRole, UUID leaseId) {
        var lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new LeaseRuleException("Lease not found"));
        if ("LANDLORD".equals(actorRole)) {
            requireOwnedRoom(actorUserId, lease.getRoomId());
        } else if (!isRepresentative(actorUserId, leaseId)) {
            throw new LeaseRuleException("Lease is not available to this user");
        }
        return LeaseView.from(lease);
    }

    private boolean isRepresentative(UUID userId, UUID leaseId) {
        var tenant = tenantRepository.findByUserId(userId);
        return tenant.isPresent() && leaseTenantRepository.findAllByIdLeaseId(leaseId).stream()
                .anyMatch(link -> link.getId().getTenantId().equals(tenant.get().getId())
                        && "REPRESENTATIVE".equals(link.getRelationship()));
    }

    private LeaseJpaEntity requireOwnedLease(UUID ownerUserId, UUID leaseId) {
        var lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new LeaseRuleException("Lease not found"));
        requireOwnedRoom(ownerUserId, lease.getRoomId());
        return lease;
    }

    private void requireOwnedRoom(UUID ownerUserId, UUID roomId) {
        if (roomRepository.findByIdAndPropertyOwnerUserId(roomId, ownerUserId).isEmpty()) {
            throw new LeaseRuleException("Room is not owned by this user");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new LeaseRuleException("endDate must be after startDate");
        }
    }

    public record LeaseView(UUID id, UUID roomId, LocalDate startDate, LocalDate endDate,
            BigDecimal rentAmount, BigDecimal depositAmount, String status) {
        static LeaseView from(LeaseJpaEntity lease) {
            return new LeaseView(lease.getId(), lease.getRoomId(), lease.getStartDate(), lease.getEndDate(),
                    lease.getRentAmount(), lease.getDepositAmount(), lease.getStatus());
        }
    }

    public record MeterView(LocalDate readingDate, BigDecimal readingValue, String meterType) {
    }

    public static class LeaseRuleException extends RuntimeException {
        public LeaseRuleException(String message) {
            super(message);
        }
    }
}
