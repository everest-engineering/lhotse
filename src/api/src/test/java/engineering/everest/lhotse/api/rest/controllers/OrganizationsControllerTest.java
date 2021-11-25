package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.ClaimSet;
import com.c4_soft.springaddons.security.oauth2.test.annotations.StringClaim;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateOrganizationRequest;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static engineering.everest.lhotse.users.UserTestHelper.ADMIN_USER;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = { OrganizationsController.class })
@ContextConfiguration(classes = { TestApiConfig.class, OrganizationsController.class })
class OrganizationsControllerTest {

    private static final String NEW_USER_USERNAME = "new@umbrella.com";
    private static final String NEW_USER_DISPLAY_NAME = "new user";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_ORG_USER = "ORG_USER";
    private static final NewUserRequest NEW_USER_REQUEST = new NewUserRequest(NEW_USER_USERNAME, NEW_USER_DISPLAY_NAME);
    private static final Organization ORGANIZATION_1 = new Organization(fromString("53ac29ab-ecc6-431e-bde0-64440cd3dc93"),
            "organization-1", new OrganizationAddress("street-1", "city-1",
            "state-1", "country-1", "postal-1"), "website-1", "contactName", "phoneNumber", "email@company.com", false);
    private static final Organization ORGANIZATION_2 = new Organization(fromString("a29797ff-11eb-40e4-9024-30e8cca17096"),
            "organization-2", new OrganizationAddress("street-2", "city-2",
            "state-2", "country-2", "postal-2"), "website-2", "", "", "", false);
    private static final User ORG_1_USER_1 = new User(randomUUID(), ORGANIZATION_1.getId(), "user11@email.com", "new-user-display-name-11", false);
    private static final User ORG_2_USER_2 = new User(randomUUID(), ORGANIZATION_2.getId(), "user22@email.com", "new-user-display-name-22", false);
    private static final UUID organizationId = ORG_1_USER_1.getOrganizationId();

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private DtoConverter dtoConverter;
    @MockBean
    private OrganizationsService organizationsService;
    @MockBean
    private OrganizationsReadService organizationsReadService;
    @MockBean
    private UsersService usersService;
    @MockBean
    private UsersReadService usersReadService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void getOrganizationWillDelegate_WhenRequestingUserBelongsToOrganization() throws Exception {
        when(dtoConverter.convert(ORGANIZATION_1))
                .thenReturn(getOrganizationResponse());
        when(organizationsReadService.getById(organizationId)).thenReturn(ORGANIZATION_1);

        mockMvc.perform(get("/api/organizations/{organizationId}", organizationId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(organizationId.toString())));
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void updateOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(put("/api/organizations/{organizationId}", ORGANIZATION_1.getId())
                        .principal(() -> ADMIN_USER.getId().toString())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrganizationRequest(ORGANIZATION_1.getOrganizationName(), ORGANIZATION_1.getOrganizationAddress().getStreet(),
                                ORGANIZATION_1.getOrganizationAddress().getCity(), ORGANIZATION_1.getOrganizationAddress().getState(), ORGANIZATION_1.getOrganizationAddress().getCountry(), ORGANIZATION_1.getOrganizationAddress().getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                                ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress()))))
                .andExpect(status().isOk());

        verify(organizationsService).updateOrganization(ADMIN_USER.getId(), ORGANIZATION_1.getId(), ORGANIZATION_1.getOrganizationName(), ORGANIZATION_1.getOrganizationAddress().getStreet(),
                ORGANIZATION_1.getOrganizationAddress().getCity(), ORGANIZATION_1.getOrganizationAddress().getState(), ORGANIZATION_1.getOrganizationAddress().getCountry(), ORGANIZATION_1.getOrganizationAddress().getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress());
    }

    @Test
    void updateOrganization_WillFail_WhenOrganizationIdIsBlank() throws Exception {
        mockMvc.perform(put("/api/organizations/{organizationId}", "")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrganizationRequest(ORGANIZATION_1.getOrganizationName(), ORGANIZATION_1.getOrganizationAddress().getStreet(),
                                ORGANIZATION_1.getOrganizationAddress().getCity(), ORGANIZATION_1.getOrganizationAddress().getState(), ORGANIZATION_1.getOrganizationAddress().getCountry(), ORGANIZATION_1.getOrganizationAddress().getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                                ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress()))))
                .andExpect(status().isNotFound());

        verifyNoInteractions(organizationsService);
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void retrievingUserListForOrganization_WillRetrieveSub_WhenRequestingUserIsAdmin() throws Exception {
        when(dtoConverter.convert(ORG_1_USER_1))
                .thenReturn(getUserResponse());
        when(usersReadService.getUsersForOrganization(ORGANIZATION_1.getId())).thenReturn(singletonList(ORG_1_USER_1));

        mockMvc.perform(get("/api/organizations/{organizationId}/users", ORGANIZATION_1.getId())
                        .principal(() -> ORG_1_USER_1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(ORG_1_USER_1.getId().toString())))
                .andExpect(jsonPath("$.[0].organizationId", is(ORGANIZATION_1.getId().toString())))
                .andExpect(jsonPath("$.[0].displayName", is(ORG_1_USER_1.getDisplayName())))
                .andExpect(jsonPath("$.[0].email", is(ORG_1_USER_1.getUsername())));
    }

    @Test
    void creatingOrganizationUser_WillFail_WhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewUserRequest("", ORG_1_USER_1.getDisplayName()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usersService);
    }

    @Test
    void creatingOrganizationUser_WillFail_WhenDisplayNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NewUserRequest(ORG_1_USER_1.getUsername(), ""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usersService);
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void creatingOrganizationUserWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                        .principal(() -> ORG_2_USER_2.getId().toString())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(NEW_USER_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.any(String.class)));

        verify(usersService).createOrganizationUser(ORG_2_USER_2.getId(), ORGANIZATION_2.getId(), NEW_USER_USERNAME, NEW_USER_DISPLAY_NAME);
    }

    @Test
    @WithMockKeycloakAuth(otherClaims = @ClaimSet(
            stringClaims = {
                    @StringClaim(name = "organizationId", value = "a29797ff-11eb-40e4-9024-30e8cca17096"),
                    @StringClaim(name = "roles", value = ROLE_ORG_USER)
            }
    ))
    void creatingOrganizationUserWillThrow_WhenRequestingUserIsNotAdmin() {
        try {
            mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                            .principal(() -> ORG_2_USER_2.getId().toString())
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(NEW_USER_REQUEST)))
                    .andReturn();
        } catch (Exception e) {
            assert e.getCause() instanceof AccessDeniedException;
        }
    }

    private static UserResponse getUserResponse() {
        return new UserResponse(OrganizationsControllerTest.ORG_1_USER_1.getId(),
                OrganizationsControllerTest.ORG_1_USER_1.getOrganizationId(),
                OrganizationsControllerTest.ORG_1_USER_1.getUsername(),
                OrganizationsControllerTest.ORG_1_USER_1.getDisplayName(),
                OrganizationsControllerTest.ORG_1_USER_1.getEmail(),
                OrganizationsControllerTest.ORG_1_USER_1.isDisabled());
    }

    private static OrganizationResponse getOrganizationResponse() {
        return new OrganizationResponse(OrganizationsControllerTest.ORGANIZATION_1.getId(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationName(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationAddress().getStreet(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationAddress().getCity(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationAddress().getState(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationAddress().getCountry(),
                OrganizationsControllerTest.ORGANIZATION_1.getOrganizationAddress().getPostalCode(),
                OrganizationsControllerTest.ORGANIZATION_1.getWebsiteUrl(),
                OrganizationsControllerTest.ORGANIZATION_1.getContactName(),
                OrganizationsControllerTest.ORGANIZATION_1.getPhoneNumber(),
                OrganizationsControllerTest.ORGANIZATION_1.getEmailAddress(),
                OrganizationsControllerTest.ORGANIZATION_1.isDisabled());
    }
}
