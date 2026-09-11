package vn.hoaivu.rentalmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rental_management")
            .withUsername("rental")
            .withPassword("rental");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID invoiceId;

    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM invoice_lines");
        jdbcTemplate.update("DELETE FROM invoices");
        jdbcTemplate.update("DELETE FROM leases");
        jdbcTemplate.update("DELETE FROM rooms");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM users");

        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID leaseId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)",
                ownerId, "owner@example.test", passwordEncoder.encode("correct-password"), "LANDLORD", "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO properties (id, owner_user_id, name, address, status) VALUES (?, ?, ?, ?, ?)",
                propertyId, ownerId, "Khu A", "123 Test Street", "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO rooms (id, property_id, code, capacity, status) VALUES (?, ?, ?, ?, ?)",
                roomId, propertyId, "P101", 2, "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO leases (id, room_id, start_date, end_date, rent_amount, deposit_amount, "
                        + "handover_electricity, handover_water, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                leaseId, roomId, "2026-02-01", "2026-05-01", "3000000.00", "3000000.00",
                "100.000", "20.000", "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO invoices (id, lease_id, period_start, period_end, due_date, total_amount, "
                        + "status, issued_by, issued_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                invoiceId, leaseId, "2026-02-01", "2026-03-01", "2026-02-05", "3000000.00",
                "ISSUED", ownerId);
    }

    @Test
    void recordsPartialAndFinalPaymentAndReturnsOldResultOnRetry() throws Exception {
        String token = login();
        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "payment-1").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"1500000.00\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"invoiceState\":\"PARTIALLY_PAID\"")))
                .andExpect(content().string(containsString("\"remainingAmount\":1500000.00")));

        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "payment-2").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"1500000.00\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"invoiceState\":\"PAID\"")));

        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "payment-1").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"1500000.00\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"paymentId\":")));
    }

    @Test
    void rejectsOverpaymentAndDifferentPayloadForSameKey() throws Exception {
        String token = login();
        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "same-key").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"3000000.01\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "same-key").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"1000000.00\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "same-key").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"900000.00\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listsConfirmedPaymentHistory() throws Exception {
        String token = login();
        mockMvc.perform(post(paymentUrl()).header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "history-1").contentType(APPLICATION_JSON)
                        .content("{\"amount\":\"500000.00\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get(paymentUrl()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("500000.00")));
    }

    private String paymentUrl() { return "/api/v1/invoices/" + invoiceId + "/payments"; }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.test\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
