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
class LeaseIntegrationTest {

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
    private UUID roomId;
    private UUID tenantUserId;
    private UUID tenantId;

    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM meter_readings");
        jdbcTemplate.update("DELETE FROM lease_tenants");
        jdbcTemplate.update("DELETE FROM leases");
        jdbcTemplate.update("DELETE FROM tenants");
        jdbcTemplate.update("DELETE FROM rooms");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM users");

        ownerId = insertUser("owner@example.test", "LANDLORD");
        tenantUserId = insertUser("tenant@example.test", "TENANT");
        tenantId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tenants (id, user_id, full_name, phone) VALUES (?, ?, ?, ?)",
                tenantId, tenantUserId, "Tenant A", "0900000000");
        UUID propertyId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO properties (id, owner_user_id, name, address, status) VALUES (?, ?, ?, ?, ?)",
                propertyId, ownerId, "Khu A", "123 Test Street", "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO rooms (id, property_id, code, capacity, status) VALUES (?, ?, ?, ?, ?)",
                roomId, propertyId, "P101", 2, "ACTIVE");
    }

    @Test
    void activationRequiresRepresentativeAndRejectsOverlappingLease() throws Exception {
        String token = login("owner@example.test");
        String firstLeaseId = createLease(token, "2026-02-01", "2026-05-01");

        mockMvc.perform(post("/api/v1/leases/" + firstLeaseId + "/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + tenantId + "\",\"relationship\":\"REPRESENTATIVE\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/leases/" + firstLeaseId + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ACTIVE\"")));

        String overlappingLeaseId = createLease(token, "2026-04-01", "2026-06-01");
        mockMvc.perform(post("/api/v1/leases/" + overlappingLeaseId + "/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + tenantId + "\",\"relationship\":\"REPRESENTATIVE\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/leases/" + overlappingLeaseId + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void readingCannotDecreaseOrUseNonIncreasingDate() throws Exception {
        String token = login("owner@example.test");
        String leaseId = createLease(token, "2026-02-01", "2026-05-01");
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/tenants")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"tenantId\":\"" + tenantId + "\",\"relationship\":\"REPRESENTATIVE\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        addReading(token, leaseId, "2026-02-01", "100.000");
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/meter-readings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"meterType\":\"ELECTRICITY\",\"readingDate\":\"2026-02-01\",\"readingValue\":\"110.000\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/meter-readings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"meterType\":\"ELECTRICITY\",\"readingDate\":\"2026-02-02\",\"readingValue\":\"99.000\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private String createLease(String token, String startDate, String endDate) throws Exception {
        String response = mockMvc.perform(post("/api/v1/rooms/" + roomId + "/leases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate
                                + "\",\"rentAmount\":\"3000000.00\",\"depositAmount\":\"3000000.00\""
                                + ",\"handoverElectricity\":\"100.000\",\"handoverWater\":\"20.000\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return response.replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private void addReading(String token, String leaseId, String date, String value) throws Exception {
        mockMvc.perform(post("/api/v1/leases/" + leaseId + "/meter-readings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"meterType\":\"ELECTRICITY\",\"readingDate\":\"" + date
                                + "\",\"readingValue\":\"" + value + "\"}"))
                .andExpect(status().isCreated());
    }

    private UUID insertUser(String email, String role) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)",
                id, email, passwordEncoder.encode("correct-password"), role, "ACTIVE");
        return id;
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
