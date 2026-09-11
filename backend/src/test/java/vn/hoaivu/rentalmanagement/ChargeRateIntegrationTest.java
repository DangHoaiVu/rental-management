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
class ChargeRateIntegrationTest {

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

    private UUID propertyId;

    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM charge_rates");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM users");
        UUID ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)",
                ownerId, "owner@example.test", passwordEncoder.encode("correct-password"), "LANDLORD", "ACTIVE");
        jdbcTemplate.update(
                "INSERT INTO properties (id, owner_user_id, name, address, status) VALUES (?, ?, ?, ?, ?)",
                propertyId, ownerId, "Khu A", "123 Test Street", "ACTIVE");
    }

    @Test
    void selectsRateByUsagePeriodAndRejectsOverlappingVersion() throws Exception {
        String token = login();
        createRate(token, "2026-02-01", "2026-04-01", "3000.00");

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/charge-rates/effective")
                        .header("Authorization", "Bearer " + token)
                        .param("code", "ELECTRICITY")
                        .param("periodStart", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3000.00")));

        createRate(token, "2026-04-01", null, "3500.00");

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/charge-rates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(rateJson("2026-03-01", "2026-05-01", "4000.00")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMidMonthEffectiveDate() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/charge-rates")
                        .header("Authorization", "Bearer " + login())
                        .contentType(APPLICATION_JSON)
                        .content(rateJson("2026-02-15", null, "3000.00")))
                .andExpect(status().isUnprocessableEntity());
    }

    private void createRate(String token, String from, String to, String price) throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/charge-rates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(rateJson(from, to, price)))
                .andExpect(status().isCreated());
    }

    private String rateJson(String from, String to, String price) {
        String effectiveTo = to == null ? "null" : "\"" + to + "\"";
        return "{\"code\":\"ELECTRICITY\",\"name\":\"Electricity\",\"unit\":\"VND/KWH\","
                + "\"unitPrice\":\"" + price + "\",\"effectiveFrom\":\"" + from
                + "\",\"effectiveTo\":" + effectiveTo + "}";
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
