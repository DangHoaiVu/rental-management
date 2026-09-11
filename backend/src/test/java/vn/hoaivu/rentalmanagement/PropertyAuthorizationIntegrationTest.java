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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PropertyAuthorizationIntegrationTest {

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
    private UUID otherOwnerId;

    @BeforeEach
    void seedUsers() {
        jdbcTemplate.update("DELETE FROM rooms");
        jdbcTemplate.update("DELETE FROM properties");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM users");
        ownerId = insertUser("owner@example.test", "LANDLORD");
        otherOwnerId = insertUser("other-owner@example.test", "LANDLORD");
        insertUser("tenant@example.test", "TENANT");
    }

    @Test
    void landlordCanCreatePropertyButTenantCannot() throws Exception {
        String landlordToken = login("owner@example.test");
        String tenantToken = login("tenant@example.test");

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + landlordToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Khu A\",\"address\":\"123 Test Street\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + tenantToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Khu B\",\"address\":\"456 Test Street\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCannotReadAnotherOwnersProperty() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO properties (id, owner_user_id, name, address, status) VALUES (?, ?, ?, ?, ?)",
                UUID.fromString(propertyId), ownerId, "Khu A", "123 Test Street", "ACTIVE");

        mockMvc.perform(get("/api/v1/properties")
                        .header("Authorization", "Bearer " + login("other-owner@example.test")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/properties/" + propertyId)
                        .header("Authorization", "Bearer " + login("other-owner@example.test")))
                .andExpect(status().isNotFound());
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
