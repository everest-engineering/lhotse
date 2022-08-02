package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.requests.CompetitionSubmissionRequest;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
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
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CompetitionsController.class })
@ContextConfiguration(classes = { TestApiConfig.class, CompetitionsController.class })
class CompetitionsControllerTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_REGISTERED_USER = "ROLE_REGISTERED_USER";
    private static final UUID USER_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID COMPETITION_ID_1 = randomUUID();
    private static final UUID COMPETITION_ID_2 = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);
    private static final Competition COMPETITION_1 =
        new Competition(COMPETITION_ID_1, "first ending", Instant.ofEpochMilli(1), Instant.ofEpochMilli(2), Instant.ofEpochMilli(3), 1);
    private static final Competition COMPETITION_2 =
        new Competition(COMPETITION_ID_2, "second ending", Instant.ofEpochMilli(1), Instant.ofEpochMilli(4), Instant.ofEpochMilli(5), 99);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private CompetitionsService competitionsService;
    @MockBean
    private CompetitionsReadService competitionsReadService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void adminsCanCreateCompetitions() throws Exception {
        when(competitionsService.createCompetition(USER_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2))
                .thenReturn(COMPETITION_ID_1);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/competitions")
            .principal(USER_ID::toString)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CreateCompetitionRequest("description", SUBMISSIONS_OPEN_TIMESTAMP,
                SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2))))
            .andExpect(status().isCreated())
            .andExpect(content().string("\"" + COMPETITION_ID_1 + "\""));
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_REGISTERED_USER)
    void competitionsCanBeRetrieved() throws Exception {
        when(competitionsReadService.getAllCompetitionsOrderedByDescVotingEndsTimestamp())
            .thenReturn(List.of(COMPETITION_2, COMPETITION_1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/competitions")
            .principal(USER_ID::toString)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(COMPETITION_2.getId().toString()))
            .andExpect(jsonPath("$[0].description").value(COMPETITION_2.getDescription()))
            .andExpect(jsonPath("$[0].submissionsOpenTimestamp").value(COMPETITION_2.getSubmissionsOpenTimestamp().toString()))
            .andExpect(jsonPath("$[0].submissionsCloseTimestamp").value(COMPETITION_2.getSubmissionsCloseTimestamp().toString()))
            .andExpect(jsonPath("$[0].votingEndsTimestamp").value(COMPETITION_2.getVotingEndsTimestamp().toString()))
            .andExpect(jsonPath("$[0].maxEntriesPerUser").value(COMPETITION_2.getMaxEntriesPerUser()))
            .andExpect(jsonPath("$[1].id").value(COMPETITION_1.getId().toString()))
            .andExpect(jsonPath("$[1].description").value(COMPETITION_1.getDescription()))
            .andExpect(jsonPath("$[1].submissionsOpenTimestamp").value(COMPETITION_1.getSubmissionsOpenTimestamp().toString()))
            .andExpect(jsonPath("$[1].submissionsCloseTimestamp").value(COMPETITION_1.getSubmissionsCloseTimestamp().toString()))
            .andExpect(jsonPath("$[1].votingEndsTimestamp").value(COMPETITION_1.getVotingEndsTimestamp().toString()))
            .andExpect(jsonPath("$[1].maxEntriesPerUser").value(COMPETITION_1.getMaxEntriesPerUser()));
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_REGISTERED_USER)
    void photosCanBeEnteredIntoCompetitions() throws Exception {
        when(competitionsReadService.getAllCompetitionsOrderedByDescVotingEndsTimestamp())
            .thenReturn(List.of(COMPETITION_1));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/competitions/{competitionId}/submission",
            COMPETITION_1.getId().toString())
            .principal(USER_ID::toString)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CompetitionSubmissionRequest(PHOTO_ID, "much wow look"))))
            .andExpect(status().isCreated());

        verify(competitionsService).submitPhoto(USER_ID, COMPETITION_ID_1, PHOTO_ID, "much wow look");
    }
}
