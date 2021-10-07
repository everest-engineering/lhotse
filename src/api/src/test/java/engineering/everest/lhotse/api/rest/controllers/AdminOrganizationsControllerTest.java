package engineering.everest.lhotse.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
// import engineering.everest.lhotse.api.helpers.AuthContextExtension;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Disabled;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import static engineering.everest.lhotse.users.UserTestHelper.ADMIN_USER;
import static java.util.UUID.fromString;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;

@WebMvcTest(controllers = {AdminOrganizationsController.class})
@ContextConfiguration(classes = {TestApiConfig.class, AdminOrganizationsController.class})
@Import({ ServletKeycloakAuthUnitTestingSupport.UnitTestConfig.class })
@AutoConfigureMockMvc
@ActiveProfiles("keycloak")
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class AdminOrganizationsControllerTest {

    private static final String USER_USERNAME = "user@umbrella.com";
    private static final String ADMIN_USERNAME = "admin@umbrella.com";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ORGANIZATION_USER = "ORG_USER";
    private static final Organization ORGANIZATION_1 = new Organization(fromString("53ac29ab-ecc6-431e-bde0-64440cd3dc93"),
            "organization-1", new OrganizationAddress("street-1", "city-1",
            "state-1", "country-1", "postal-1"), "website-1", "contactName", "phoneNumber", "email@company.com", false);
    private static final Organization ORGANIZATION_2 = new Organization(fromString("a29797ff-11eb-40e4-9024-30e8cca17096"),
            "organization-2", new OrganizationAddress("street-2", "city-2",
            "state-2", "country-2", "postal-2"), "website-2", "", "", "", false);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationsService organizationsService;
    @MockBean
    private OrganizationsReadService organizationsReadService;
    @MockBean
    private UsersService usersService;
    @MockBean
    private UsersReadService usersReadService;

    @Test
    @Disabled
    @WithMockKeycloakAuth( authorities = {ROLE_ADMIN})
    void getOrganizationsWillRetrieveListOfOrganizations_WhenRequestingUserIsAdmin() throws Exception {
        when(organizationsReadService.getOrganizations())
                .thenReturn(newArrayList(ORGANIZATION_1, ORGANIZATION_2));

        mockMvc.perform(get("/admin/organizations").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(ORGANIZATION_1.getId().toString())))
                .andExpect(jsonPath("$.[1].id", is(ORGANIZATION_2.getId().toString())))
                .andExpect(jsonPath("$.[0].organizationName", is(ORGANIZATION_1.getOrganizationName())))
                .andExpect(jsonPath("$.[1].organizationName", is(ORGANIZATION_2.getOrganizationName())));
    }

    @Test
    @Disabled
    @WithMockKeycloakAuth( authorities = {ROLE_ADMIN})
    void creatingRegisteredOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(post("/admin/organizations")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewOrganizationRequest(ORGANIZATION_1.getOrganizationName(), ORGANIZATION_1.getOrganizationAddress().getStreet(),
                        ORGANIZATION_1.getOrganizationAddress().getCity(), ORGANIZATION_1.getOrganizationAddress().getState(), ORGANIZATION_1.getOrganizationAddress().getCountry(), ORGANIZATION_1.getOrganizationAddress().getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                        ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress()))))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.any(String.class)));

        verify(organizationsService).createRegisteredOrganization(ADMIN_USER.getId(), ORGANIZATION_1.getOrganizationName(), ORGANIZATION_1.getOrganizationAddress().getStreet(),
                ORGANIZATION_1.getOrganizationAddress().getCity(), ORGANIZATION_1.getOrganizationAddress().getState(), ORGANIZATION_1.getOrganizationAddress().getCountry(), ORGANIZATION_1.getOrganizationAddress().getPostalCode(), ORGANIZATION_1.getWebsiteUrl(),
                ORGANIZATION_1.getContactName(), ORGANIZATION_1.getPhoneNumber(), ORGANIZATION_1.getEmailAddress());
    }

    @Test
    @Disabled
    @WithMockKeycloakAuth( authorities = {ROLE_ADMIN})
    void disableOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(delete("/admin/organizations/{organizationId}", ORGANIZATION_2.getId()))
                .andExpect(status().isOk());

        verify(organizationsService).disableOrganization(ADMIN_USER.getId(), ORGANIZATION_2.getId());
    }

    @Test
    @Disabled
    @WithMockKeycloakAuth( authorities = {ROLE_ADMIN})
    void enableOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(post("/admin/organizations/{organizationId}", ORGANIZATION_2.getId()))
                .andExpect(status().isOk());

        verify(organizationsService).enableOrganization(ADMIN_USER.getId(), ORGANIZATION_2.getId());
    }
}
