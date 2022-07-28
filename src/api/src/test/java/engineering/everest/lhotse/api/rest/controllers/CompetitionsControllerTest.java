package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.competitions.services.CompetitionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CompetitionsController.class })
@ContextConfiguration(classes = { TestApiConfig.class, CompetitionsController.class })
class CompetitionsControllerTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private CompetitionsService competitionsService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void uploadPhotosWillPersistAndRegisterUpload() throws Exception {
        when(competitionsService.createCompetition(USER_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP, SUBMISSIONS_CLOSE_TIMESTAMP,
            VOTING_ENDS_TIMESTAMP, 2))
                .thenReturn(COMPETITION_ID);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/competitions")
            .principal(USER_ID::toString)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CreateCompetitionRequest("description", SUBMISSIONS_OPEN_TIMESTAMP,
                SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2))))
            .andExpect(status().isCreated())
            .andExpect(content().string("\"" + COMPETITION_ID + "\""));
    }
}
