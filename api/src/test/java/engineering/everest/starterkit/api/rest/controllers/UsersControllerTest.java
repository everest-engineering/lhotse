package engineering.everest.starterkit.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.starterkit.api.config.TestApiConfig;
import engineering.everest.starterkit.api.helpers.AuthContextExtension;
import engineering.everest.starterkit.api.helpers.MockAuthenticationContextProvider;
import engineering.everest.starterkit.api.rest.requests.UpdateUserRequest;
import engineering.everest.starterkit.axon.common.domain.Identifiable;
import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.axon.common.services.ReadService;
import engineering.everest.starterkit.axon.common.services.ReadServiceProvider;
import engineering.everest.starterkit.axon.users.services.UsersReadService;
import engineering.everest.starterkit.axon.users.services.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestApiConfig.class, UsersController.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class, AuthContextExtension.class})
class UsersControllerTest {

    private static final UUID USER_ID_1 = randomUUID();
    private static final UUID USER_ID_2 = randomUUID();
    private static final UUID ORGANIZATION_ID_1 = randomUUID();
    private static final UUID ORGANIZATION_ID_2 = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final User ORG_1_USER_1 = new User(USER_ID_1, ORGANIZATION_ID_1, "org-1-user-1", "org-1-user-1-display");
    private static final User ORG_2_USER_1 = new User(USER_ID_2, ORGANIZATION_ID_2, "org-2-user-1", "org-2-user-1-display");
    private static final String USER_USERNAME = "user@umbrella.com";
    private static final String ADMIN_USERNAME = "admin@umbrella.com";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_ORGANIZATION_ADMIN = "ROLE_ORGANIZATION_ADMIN";
    private static final String ROLE_ORGANIZATION_USER = "ROLE_ORGANIZATION_USER";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReadServiceProvider readServiceProvider;

    @MockBean
    private UsersService usersService;
    @MockBean
    private UsersReadService usersReadService;

    @BeforeEach
    void setUp() {
        Mockito.<ReadService<? extends Identifiable>>when(readServiceProvider.getService(User.class.getSimpleName()))
                .thenReturn(usersReadService);
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void retrievingGlobalUserList_WillDelegate() throws Exception {
        when(usersReadService.getUsers()).thenReturn(asList(ORG_1_USER_1, ORG_2_USER_1));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(ORG_1_USER_1.getId().toString())))
                .andExpect(jsonPath("$.[1].id", is(ORG_2_USER_1.getId().toString())))
                .andExpect(jsonPath("$.[0].organizationId", is(ORGANIZATION_ID_1.toString())))
                .andExpect(jsonPath("$.[1].organizationId", is(ORGANIZATION_ID_2.toString())))
                .andExpect(jsonPath("$.[0].displayName", is(ORG_1_USER_1.getDisplayName())))
                .andExpect(jsonPath("$.[1].displayName", is(ORG_2_USER_1.getDisplayName())))
                .andExpect(jsonPath("$.[0].email", is(ORG_1_USER_1.getUsername())))
                .andExpect(jsonPath("$.[1].email", is(ORG_2_USER_1.getUsername())));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void getUserDetails_WillRetrieveSingleUserDetails() throws Exception {
        when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);

        mockMvc.perform(get("/api/users/{userId}", ORG_2_USER_1.getId()).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(ORG_2_USER_1.getId().toString())))
                .andExpect(jsonPath("$.organizationId", is(ORGANIZATION_ID_2.toString())))
                .andExpect(jsonPath("$.displayName", is(ORG_2_USER_1.getDisplayName())))
                .andExpect(jsonPath("$.email", is(ORG_2_USER_1.getUsername())));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void updateUserDetailsWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
        when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);
        mockMvc.perform(put("/api/users/{userId}", ORG_2_USER_1.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change", "password-change"))))
                .andExpect(status().isOk());

        verify(usersService).updateUser(ADMIN_ID, ORG_2_USER_1.getId(), "email-change",
                "display-name-change", "password-change");
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ORGANIZATION_ADMIN)
    void updateUserDetailsWillDelegate_WhenRequestingUserIsAdminOfOrganization() throws Exception {
        var authUser = MockAuthenticationContextProvider.getAuthUser();
        var aUser = new User(randomUUID(), authUser.getOrganizationId(), USER_USERNAME, "user");
        when(usersReadService.getById(aUser.getId())).thenReturn(aUser);
        mockMvc.perform(put("/api/users/{userId}", aUser.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change", "password-change"))))
                .andExpect(status().isOk());

        verify(usersService).updateUser(authUser.getId(), aUser.getId(), "email-change",
                "display-name-change", "password-change");
    }

    @Test
    @WithMockUser(username = USER_USERNAME, roles = ROLE_ORGANIZATION_USER)
    void updateUserDetailsWillDelegate_WhenRequestingUserIsTargetUser() throws Exception {
        var authUser = MockAuthenticationContextProvider.getAuthUser();
        when(usersReadService.getById(authUser.getId())).thenReturn(authUser);
        mockMvc.perform(put("/api/users/{userId}", authUser.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change", "password-change"))))
                .andExpect(status().isOk());

        verify(usersService).updateUser(authUser.getId(), authUser.getId(), "email-change",
                "display-name-change", "password-change");
    }

    @Test
    @WithMockUser(username = USER_USERNAME, roles = ROLE_ORGANIZATION_USER)
    void getUserById_WillDelegate() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        User targetUser = new User(randomUUID(), authUser.getOrganizationId(), "other@umbrella.com", "other");
        when(usersReadService.getById(targetUser.getId())).thenReturn(targetUser);
        mockMvc.perform(get("/api/users/{userId}", targetUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is(targetUser.getDisplayName())))
                .andExpect(jsonPath("$.id", is(targetUser.getId().toString())))
                .andExpect(jsonPath("$.organizationId", is(targetUser.getOrganizationId().toString())));
    }

    @Test
    @WithMockUser(username = USER_USERNAME, roles = ROLE_ORGANIZATION_USER)
    void getUserOfOtherOrganization_WillThrow() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        User targetUser = new User(randomUUID(), randomUUID(), "other@umbrella.com", "other");
        when(usersReadService.getById(targetUser.getId())).thenReturn(targetUser);
        mockMvc.perform(get("/api/users/{userId}", targetUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ADMIN)
    void getUserOfAnyOrganization_WillDelegateForAdmin() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        User targetUser = new User(randomUUID(), randomUUID(), "other@umbrella.com", "other");
        when(usersReadService.getById(targetUser.getId())).thenReturn(targetUser);
        mockMvc.perform(get("/api/users/{userId}", targetUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is(targetUser.getDisplayName())))
                .andExpect(jsonPath("$.id", is(targetUser.getId().toString())))
                .andExpect(jsonPath("$.organizationId", is(targetUser.getOrganizationId().toString())));
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ORGANIZATION_ADMIN)
    void updateUser_WillDelegate() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        UUID targetUserId = randomUUID();
        when(usersReadService.getById(targetUserId)).thenReturn(
                new User(targetUserId, authUser.getOrganizationId(), "some@umbrella.com", ""));
        mockMvc.perform(put("/api/users/{userId}", targetUserId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("new", null, null))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = ADMIN_USERNAME, roles = ROLE_ORGANIZATION_ADMIN)
    void updateUserOfOtherOrganization_WillFail() throws Exception {
        UUID targetUserId = randomUUID();
        when(usersReadService.getById(targetUserId)).thenReturn(
                new User(targetUserId, randomUUID(), "some@ghostbusters.com", ""));
        mockMvc.perform(put("/api/users/{userId}", targetUserId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("new", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void willGetAllUsers_WhenRequestingUserIsAdmin() {
        // TODO
    }

    @Test
    void willGetUsersOfOrganization_WhenRequestingUserIsNotAdmin() {
        // TODO
    }

    @Test
    void updateUserAccountStatus_WillDelegate() {
        // TODO
    }
}
