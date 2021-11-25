package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static java.util.UUID.fromString;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static engineering.everest.lhotse.users.UserTestHelper.ADMIN_USER;

@WebMvcTest(controllers = { AdminOrganizationsController.class })
@ContextConfiguration(classes = { TestApiConfig.class, AdminOrganizationsController.class })
public class AdminOrganizationsControllerTest {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final Organization ORGANIZATION_1 = new Organization(
            fromString("53ac29ab-ecc6-431e-bde0-64440cd3dc93"), "organization-1",
            new OrganizationAddress("street-1", "city-1", "state-1", "country-1", "postal-1"), "website-1",
            "contactName", "phoneNumber", "email@company.com", false);
    private static final Organization ORGANIZATION_2 = new Organization(
            fromString("a29797ff-11eb-40e4-9024-30e8cca17096"), "organization-2",
            new OrganizationAddress("street-2", "city-2", "state-2", "country-2", "postal-2"), "website-2", "", "", "",
            false);

    private MockMvc mockMvc;
    private Principal principal;

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

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.principal = () -> ADMIN_USER.getId().toString();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void getOrganizationsWillRetrieveListOfOrganizations_WhenRequestingUserIsAdmin() throws Exception {
        when(dtoConverter.convert(ORGANIZATION_1))
                .thenReturn(getOrganizationResponse(ORGANIZATION_1));
        when(dtoConverter.convert(ORGANIZATION_2))
                .thenReturn(getOrganizationResponse(ORGANIZATION_2));
        when(organizationsReadService.getOrganizations()).thenReturn(newArrayList(ORGANIZATION_1, ORGANIZATION_2));

        mockMvc.perform(get("/admin/organizations")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(ORGANIZATION_1.getId().toString())))
                .andExpect(jsonPath("$.[1].id", is(ORGANIZATION_2.getId().toString())))
                .andExpect(jsonPath("$.[0].organizationName", is(ORGANIZATION_1.getOrganizationName())))
                .andExpect(jsonPath("$.[1].organizationName", is(ORGANIZATION_2.getOrganizationName())));
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void disableOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(delete("/admin/organizations/{organizationId}", ORGANIZATION_2.getId())
                        .principal(principal))
                .andExpect(status().isOk());

        verify(organizationsService).disableOrganization(ADMIN_USER.getId(), ORGANIZATION_2.getId());
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
    void enableOrganizationWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        mockMvc.perform(post("/admin/organizations/{organizationId}", ORGANIZATION_2.getId())
                        .principal(principal))
                .andExpect(status().isOk());

        verify(organizationsService).enableOrganization(ADMIN_USER.getId(), ORGANIZATION_2.getId());
    }

    private static OrganizationResponse getOrganizationResponse(Organization organization) {
        return new OrganizationResponse(organization.getId(),
                organization.getOrganizationName(),
                organization.getOrganizationAddress().getStreet(),
                organization.getOrganizationAddress().getCity(),
                organization.getOrganizationAddress().getState(),
                organization.getOrganizationAddress().getCountry(),
                organization.getOrganizationAddress().getPostalCode(),
                organization.getWebsiteUrl(),
                organization.getContactName(),
                organization.getPhoneNumber(),
                organization.getEmailAddress(),
                organization.isDisabled());
    }
}
