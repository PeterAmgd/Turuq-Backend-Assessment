package turuq.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration tests: real Spring context, real security filter chain, real
 * (embedded) MongoDB. These exercise the seeded login account, the JWT auth requirement
 * on /users/**, and the complete CRUD + validation + error-handling behavior end to end -
 * as opposed to UserServiceTest / JwtUtilTest, which test individual units in isolation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void login() throws Exception {
        // Confirms the DataSeeder-created account (user / user123) is actually usable.
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user","password":"user123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        token = body.get("token").asText();
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fullCrudLifecycle_createReadUpdateDelete() throws Exception {
        // CREATE
        MvcResult createResult = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada Lovelace","email":"ada@example.com","age":36}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // READ (by id)
        mockMvc.perform(get("/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));

        // READ (list, filtered by age)
        mockMvc.perform(get("/users?age=36").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].email").value("ada@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // UPDATE
        mockMvc.perform(put("/users/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ada L.","email":"ada@example.com","age":37}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ada L."))
                .andExpect(jsonPath("$.age").value(37));

        // DELETE
        mockMvc.perform(delete("/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Confirm it's actually gone
        mockMvc.perform(get("/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createUser_withDuplicateEmail_returns409() throws Exception {
        String payload = """
                {"name":"Grace Hopper","email":"grace@example.com","age":40}
                """;

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createUser_withMissingRequiredFields_returns400WithDetails() throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"age":30}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("details").toString()).contains("name").contains("email");
    }

    @Test
    void getUserById_withMalformedId_returns400() throws Exception {
        mockMvc.perform(get("/users/not-a-valid-object-id")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
