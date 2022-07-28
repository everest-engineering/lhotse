package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
import engineering.everest.lhotse.users.services.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { UsersController.class })
@ContextConfiguration(classes = { TestApiConfig.class, UsersController.class })
class UsersControllerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UsersService usersService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void deleteAndForgetUser_WillDelegate() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/forget", USER_ID)
            .principal(ADMIN_ID::toString)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new DeleteAndForgetUserRequest("Submitted GDPR request"))))
            .andExpect(status().isOk());

        verify(usersService).deleteAndForgetUser(ADMIN_ID, USER_ID, "Submitted GDPR request");
    }
}
