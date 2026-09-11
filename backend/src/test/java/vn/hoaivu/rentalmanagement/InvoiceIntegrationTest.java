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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class InvoiceIntegrationTest {

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

    private UUID ownerId;
    private UUID leaseId;

    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM invoice_lines");
        jdbcTemplate.update("DELETE FROM invoices");
        jdbcTemplate.update("DELETE FROM charge_rates");
        jdbcTemplate.update("DELETE FROM leases");
        jdbcTemplate.update("DELETE FROM rooms");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM users");

        ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        leaseId = UUID.randomUUID();
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
                "INSERT INTO charge_rates (id, property_id, code, name, unit, unit_price, effective_from) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), propertyId, "ELECTRICITY", "Electricity", "VND/KWH", "3000.00", "2026-02-01");
    }

    @Test
    void issueSnapshotsRateSelectedAtPeriodStart() throws Exception {
        String token = login();
        String draft = "{\"periodStart\":\"2026-02-01\",\"dueDate\":\"2026-02-05\",\"lines\":["
                + "{\"meterType\":null,\"meterStartReadingId\":null,\"meterEndReadingId\":null,"
                + "\"chargeCode\":\"ELECTRICITY\",\"description\":\"Electricity\",\"quantity\":\"10.000\"}]}";
        String response = mockMvc.perform(post("/api/v1/leases/" + leaseId + "/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(draft))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String invoiceId = response.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(post("/api/v1/invoices/" + invoiceId + "/issue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"totalAmount\":3030000.00")))
                .andExpect(content().string(containsString("\"unitPrice\":3000.00")));
    }

    @Test
    void rejectsPartialMonthAndDuplicatePeriod() throws Exception {
        String token = login();
        String partial = "{\"periodStart\":\"2026-01-01\",\"dueDate\":\"2026-01-05\",\"lines\":[]}";
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(partial))
                .andExpect(status().isUnprocessableEntity());

        String valid = "{\"periodStart\":\"2026-02-01\",\"dueDate\":\"2026-02-05\",\"lines\":[]}";
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(valid))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(valid))
                .andExpect(status().isConflict());
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.test\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
