package engineering.everest.lhotse.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.helpers.AuthContextExtension;
import engineering.everest.lhotse.api.helpers.MockAuthenticationContextProvider;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.RegisterOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateOrganizationRequest;
import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import engineering.everest.lhotse.registrations.services.PendingRegistrationsService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static engineering.everest.lhotse.users.UserTestHelper.ADMIN_USER;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestApiConfig.class, OrganizationsController.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class, AuthContextExtension.class})
class OrganizationsControllerTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final String USER_USERNAME = "user@umbrella.com";
    private static final String ADMIN_USERNAME = "admin@umbrella.com";
    private static final String RAW_PASSWORD = "secret";
    private static final String NEW_USER_USERNAME = "new@umbrella.com";
    private static final String NEW_USER_DISPLAY_NAME = "new user";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ORGANIZATION_USER = "ORG_USER";
    private static final NewUserRequest NEW_USER_REQUEST = new NewUserRequest(NEW_USER_USERNAME, RAW_PASSWORD, NEW_USER_DISPLAY_NAME);
    private static final Organization ORGANIZATION_1 = new Organization(fromString("53ac29ab-ecc6-431e-bde0-64440cd3dc93"),
            "organization-1", new OrganizationAddress("street-1", "city-1",
            "state-1", "country-1", "postal-1"), "website-1", "contactName", "phoneNumber", "email@company.com", false);
    private static final Organization ORGANIZATION_2 = new Organization(fromString("a29797ff-11eb-40e4-9024-30e8cca17096"),
            "organization-2", new OrganizationAddress("street-2", "city-2",
            "state-2", "country-2", "postal-2"), "website-2", "", "", "", false);
    private static final User ORG_1_USER_1 = new User(randomUUID(), ORGANIZATION_1.getId(), "user11@email.com", "new-user-display-name-11", false);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationsService organizationsService;
    @MockBean
    private PendingRegistrationsService pendingRegistrationsService;
    @MockBean
    private OrganizationsReadService organizationsReadService;
    @MockBean
    private UsersService usersService;
    @MockBean
    private UsersReadService usersReadService;
    @MockBean
    private RandomFieldsGenerator randomFieldsGenerator;

    @Test
    @WithMockUser(username = USER_USERNAME, roles = ROLE_ORGANIZATION_USER)
    void getOrganizationWillDelegate_WhenRequestingUserBelongsToOrganization() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        when(organizationsReadService.getById(authUser.getOrganizationId()))
                .thenReturn(new Organization(authUser.getOrganizationId(), "demo",
                        new OrganizationAddress("", "", "", "", ""),
                        "", "", "", "", false));
        mockMvc.perform(get("/api/organizations/{organizationId}", authUser.getOrganizationId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(authUser.getOrganizationId().toString())));
    }

    @Test
    void registerOrganizationWillDelegate() throws Exception {
        when(randomFieldsGenerator.genRandomUUID())
                .thenReturn(ORGANIZATION_1.getId())
                .thenReturn(USER_ID);

        OrganizationAddress address = ORGANIZATION_1.getOrganizationAddress();

        mockMvc.perform(post("/api/organizations/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterOrganizationRequest(ORGANIZATION_1.getOrganizationName(), address.getStreet(),
                        address.getCity(), address.getState(), address.getCountry(), address.getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                        ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress(), RAW_PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.newOrganizationId", is(ORGANIZATION_1.getId().toString())))
                .andExpect(jsonPath("$.newUserId", is(USER_ID.toString())));

        verify(pendingRegistrationsService).registerOrganization(ORGANIZATION_1.getId(), USER_ID, ORGANIZATION_1.getOrganizationName(),
                address.getStreet(), address.getCity(), address.getState(), address.getCountry(), address.getPostalCode(),
                ORGANIZATION_1.getWebsiteUrl(), ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress(), RAW_PASSWORD);
        verifyNoInteractions(usersService);
    }

    @Test
    void confirmOrganizationRegistration() throws Exception {
        UUID confirmationCode = randomUUID();
        mockMvc.perform(get("/api/organizations/{organizationId}/register/{confirmationCode}", ORGANIZATION_ID, confirmationCode))
                .andExpect(status().isOk());

        verify(pendingRegistrationsService).confirmOrganizationRegistrationEmail(ORGANIZATION_ID, confirmationCode);
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void updateOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(put("/api/organizations/{organizationId}", ORGANIZATION_1.getId())
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
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
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
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void retrievingUserListForOrganization_WillRetrieveSub_WhenRequestingUserIsAdmin() throws Exception {
        when(usersReadService.getUsersForOrganization(ORGANIZATION_1.getId())).thenReturn(singletonList(ORG_1_USER_1));

        mockMvc.perform(get("/api/organizations/{organizationId}/users", ORGANIZATION_1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(ORG_1_USER_1.getId().toString())))
                .andExpect(jsonPath("$.[0].organizationId", is(ORGANIZATION_1.getId().toString())))
                .andExpect(jsonPath("$.[0].displayName", is(ORG_1_USER_1.getDisplayName())))
                .andExpect(jsonPath("$.[0].email", is(ORG_1_USER_1.getUsername())));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void creatingOrganizationUser_WillFail_WhenEmailIsBlank() throws Exception {
        var rawPassword = "secret";
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewUserRequest("", rawPassword, ORG_1_USER_1.getDisplayName()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usersService);
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void creatingOrganizationUser_WillFail_WhenDisplayNameIsBlank() throws Exception {
        var rawPassword = "secret";
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewUserRequest(ORG_1_USER_1.getUsername(), rawPassword, ""))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usersService);
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void creatingOrganizationUser_WillFail_WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_2.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewUserRequest(ORG_1_USER_1.getUsername(), "", ORG_1_USER_1.getDisplayName()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usersService);
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void creatingOrganizationUserWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        mockMvc.perform(post("/api/organizations/{organizationId}/users", ORGANIZATION_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(NEW_USER_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.any(String.class)));

        verify(usersService).createUser(authUser.getId(), ORGANIZATION_ID, NEW_USER_USERNAME, NEW_USER_DISPLAY_NAME, RAW_PASSWORD);
    }

    @Test
    @WithMockUser(username = USER_USERNAME, roles = ROLE_ORGANIZATION_USER)
    void creatingOrganizationUserWillThrow_WhenRequestingUserIsNotAdmin() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        mockMvc.perform(post("/api/organizations/{organizationId}/users", authUser.getOrganizationId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(NEW_USER_REQUEST)))
                .andExpect(status().isForbidden());
    }
}
