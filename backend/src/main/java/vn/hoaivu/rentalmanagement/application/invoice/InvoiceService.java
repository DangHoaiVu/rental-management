package vn.hoaivu.rentalmanagement.application.invoice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.hoaivu.rentalmanagement.infrastructure.billing.persistence.ChargeRateJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.billing.persistence.ChargeRateJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceLineJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.invoice.persistence.InvoiceLineJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.lease.persistence.LeaseJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.meter.persistence.MeterReadingJpaEntity;
import vn.hoaivu.rentalmanagement.infrastructure.meter.persistence.MeterReadingJpaRepository;
import vn.hoaivu.rentalmanagement.infrastructure.room.persistence.RoomJpaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceJpaRepository invoiceRepository;
    private final InvoiceLineJpaRepository lineRepository;
    private final LeaseJpaRepository leaseRepository;
    private final RoomJpaRepository roomRepository;
    private final ChargeRateJpaRepository rateRepository;
    private final MeterReadingJpaRepository readingRepository;
    private final Clock clock = Clock.systemUTC();

    public InvoiceService(InvoiceJpaRepository invoiceRepository, InvoiceLineJpaRepository lineRepository,
            LeaseJpaRepository leaseRepository, RoomJpaRepository roomRepository,
            ChargeRateJpaRepository rateRepository, MeterReadingJpaRepository readingRepository) {
        this.invoiceRepository = invoiceRepository;
        this.lineRepository = lineRepository;
        this.leaseRepository = leaseRepository;
        this.roomRepository = roomRepository;
        this.rateRepository = rateRepository;
        this.readingRepository = readingRepository;
    }

    @Transactional
    public InvoiceView createDraft(UUID ownerUserId, UUID leaseId, LocalDate periodStart, LocalDate dueDate,
            List<LineRequest> requestedLines) {
        LeaseJpaEntity lease = requireOwnedLease(ownerUserId, leaseId);
        validateMonth(periodStart);
        LocalDate periodEnd = periodStart.plusMonths(1);
        if (!"ACTIVE".equals(lease.getStatus())) {
            throw new InvoiceRuleException("Only ACTIVE leases can receive invoices");
        }
        if (!isFullPeriodWithinLease(lease, periodStart, periodEnd)) {
            throw new InvoiceRuleException("Invoice period must be fully within the lease; monthly proration is unsupported");
        }
        if (dueDate.isBefore(periodStart)) {
            throw new InvoiceRuleException("dueDate cannot be before periodStart");
        }
        InvoiceJpaEntity invoice;
        try {
            invoice = invoiceRepository.save(new InvoiceJpaEntity(UUID.randomUUID(), leaseId,
                    periodStart, periodEnd, dueDate));
            lineRepository.save(new InvoiceLineJpaEntity(UUID.randomUUID(), invoice.getId(), 1,
                    null, null, null, null, "RENT", "Rent", BigDecimal.ONE,
                    BigDecimal.ZERO.setScale(2), "VND/MONTH", BigDecimal.ZERO.setScale(2)));
            int lineNo = 2;
            for (LineRequest request : requestedLines) {
                if ("RENT".equals(request.chargeCode())) {
                    throw new InvoiceRuleException("RENT line is generated from the lease rent amount");
                }
                lineRepository.save(new InvoiceLineJpaEntity(UUID.randomUUID(), invoice.getId(), lineNo++,
                        request.meterType(), request.meterStartReadingId(), request.meterEndReadingId(), null,
                        request.chargeCode(), request.description(), request.quantity(), BigDecimal.ZERO.setScale(2),
                        "PENDING", BigDecimal.ZERO.setScale(2)));
            }
            invoiceRepository.flush();
            return InvoiceView.from(invoice, lineRepository.findAllByInvoiceIdOrderByLineNo(invoice.getId()));
        } catch (DataIntegrityViolationException exception) {
            throw new InvoiceRuleException("An invoice already exists for this lease and period");
        }
    }

    @Transactional
    public InvoiceView issue(UUID ownerUserId, UUID invoiceId) {
        InvoiceJpaEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceRuleException("Invoice not found"));
        LeaseJpaEntity lease = requireOwnedLease(ownerUserId, invoice.getLeaseId());
        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new InvoiceRuleException("Only DRAFT invoices can be issued");
        }
        if (!isFullPeriodWithinLease(lease, invoice.getPeriodStart(), invoice.getPeriodEnd())) {
            throw new InvoiceRuleException("Invoice period must be fully within the lease");
        }
        UUID propertyId = roomRepository.findById(lease.getRoomId()).orElseThrow()
                .getProperty().getId();
        var lines = lineRepository.findAllByInvoiceIdOrderByLineNo(invoiceId);
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (InvoiceLineJpaEntity line : lines) {
            if ("RENT".equals(line.getChargeCode())) {
                total = snapshotLine(line, "Rent", BigDecimal.ONE, lease.getRentAmount(), "VND/MONTH", null, total);
                continue;
            }
            ChargeRateJpaEntity rate = rateRepository.findEffectiveRate(
                            propertyId, line.getChargeCode(), invoice.getPeriodStart())
                    .orElseThrow(() -> new InvoiceRuleException("No rate for code " + line.getChargeCode()
                            + " at the invoice period start"));
            BigDecimal quantity = line.getQuantity();
            if (line.getMeterStartReadingId() != null) {
                MeterReadingJpaEntity start = readingRepository.findById(line.getMeterStartReadingId())
                        .orElseThrow(() -> new InvoiceRuleException("Meter start reading not found"));
                MeterReadingJpaEntity end = readingRepository.findById(line.getMeterEndReadingId())
                        .orElseThrow(() -> new InvoiceRuleException("Meter end reading not found"));
                validateMeterPair(line, invoice.getLeaseId(), start, end);
                quantity = end.getReadingValue().subtract(start.getReadingValue());
            }
            BigDecimal amount = roundedLineAmount(quantity, rate.getUnitPrice());
            line.snapshot(rate.getName(), quantity, rate.getUnitPrice(), rate.getUnit(), rate.getId(), amount);
            total = total.add(amount);
        }
        invoice.issue(total.setScale(2), ownerUserId, clock.instant());
        invoiceRepository.flush();
        return InvoiceView.from(invoice, lines);
    }

    private BigDecimal snapshotLine(InvoiceLineJpaEntity line, String description, BigDecimal quantity,
            BigDecimal unitPrice, String unit, UUID sourceRateId, BigDecimal total) {
        BigDecimal amount = roundedLineAmount(quantity, unitPrice);
        line.snapshot(description, quantity, unitPrice, unit, sourceRateId, amount);
        return total.add(amount);
    }

    private void validateMeterPair(InvoiceLineJpaEntity line, UUID leaseId,
            MeterReadingJpaEntity start, MeterReadingJpaEntity end) {
        if (!leaseId.equals(start.getLeaseId()) || !leaseId.equals(end.getLeaseId())
                || !start.getMeterType().equals(end.getMeterType())
                || !start.getMeterType().equals(line.getMeterType())
                || !start.getReadingDate().isBefore(end.getReadingDate())) {
            throw new InvoiceRuleException("Meter readings must share lease/type and be in order");
        }
    }

    private BigDecimal roundedLineAmount(BigDecimal quantity, BigDecimal unitPrice) {
        return quantity.multiply(unitPrice).setScale(0, RoundingMode.HALF_UP).setScale(2);
    }

    private boolean isFullPeriodWithinLease(LeaseJpaEntity lease, LocalDate start, LocalDate end) {
        return !start.isBefore(lease.getStartDate())
                && (lease.getEndDate() == null || !end.isAfter(lease.getEndDate()));
    }

    private LeaseJpaEntity requireOwnedLease(UUID ownerUserId, UUID leaseId) {
        LeaseJpaEntity lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new InvoiceRuleException("Lease not found"));
        if (roomRepository.findByIdAndPropertyOwnerUserId(lease.getRoomId(), ownerUserId).isEmpty()) {
            throw new InvoiceRuleException("Lease is not owned by this user");
        }
        return lease;
    }

    private void validateMonth(LocalDate periodStart) {
        if (!periodStart.equals(periodStart.withDayOfMonth(1))) {
            throw new InvoiceRuleException("periodStart must be the first day of a month");
        }
    }

    public record LineRequest(String meterType, UUID meterStartReadingId, UUID meterEndReadingId,
            String chargeCode, String description, BigDecimal quantity) {
    }

    public record InvoiceView(UUID id, UUID leaseId, LocalDate periodStart, LocalDate periodEnd,
            LocalDate dueDate, BigDecimal totalAmount, String status, List<LineView> lines) {
        static InvoiceView from(InvoiceJpaEntity invoice, List<InvoiceLineJpaEntity> lines) {
            return new InvoiceView(invoice.getId(), invoice.getLeaseId(), invoice.getPeriodStart(),
                    invoice.getPeriodEnd(), invoice.getDueDate(), invoice.getTotalAmount(), invoice.getStatus(),
                    lines.stream().map(line -> new LineView(line.getLineNo(), line.getChargeCode(),
                            line.getDescription(), line.getQuantity(), line.getUnitPrice(), line.getUnit(),
                            line.getAmount())).toList());
        }
    }

    public record LineView(int lineNo, String chargeCode, String description, BigDecimal quantity,
            BigDecimal unitPrice, String unit, BigDecimal amount) {
    }

    public static class InvoiceRuleException extends RuntimeException {
        public InvoiceRuleException(String message) { super(message); }
    }
}
