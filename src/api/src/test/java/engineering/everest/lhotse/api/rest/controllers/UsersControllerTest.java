// package engineering.everest.lhotse.api.rest.controllers;
//
// import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import engineering.everest.lhotse.api.config.TestApiConfig;
// import engineering.everest.lhotse.api.rest.converters.DtoConverter;
// import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
// import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
// import engineering.everest.lhotse.api.rest.responses.UserResponse;
// import engineering.everest.lhotse.common.domain.Identifiable;
// import engineering.everest.lhotse.common.domain.User;
// import engineering.everest.lhotse.common.services.ReadService;
// import engineering.everest.lhotse.common.services.ReadServiceProvider;
// import engineering.everest.lhotse.users.services.UsersReadService;
// import engineering.everest.lhotse.users.services.UsersService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.context.WebApplicationContext;
//
// import java.util.Set;
// import java.util.UUID;
//
// import static engineering.everest.lhotse.common.domain.Role.ADMIN;
// import static engineering.everest.lhotse.users.UserTestHelper.ADMIN_USER;
// import static java.util.Arrays.asList;
// import static java.util.UUID.randomUUID;
// import static org.hamcrest.Matchers.is;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.springframework.http.MediaType.APPLICATION_JSON;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
// @WebMvcTest(controllers = { UsersController.class })
// @ContextConfiguration(classes = { TestApiConfig.class, UsersController.class })
// class UsersControllerTest {
//
// private static final UUID USER_ID_1 = randomUUID();
// private static final UUID USER_ID_2 = randomUUID();
// private static final UUID ORGANIZATION_ID_1 = randomUUID();
// private static final UUID ORGANIZATION_ID_2 = randomUUID();
// private static final User ORG_1_USER_1 = new User(USER_ID_1, ORGANIZATION_ID_1, "org-1-user-1-display", "org-1-user-1");
// private static final User ORG_2_USER_1 = new User(USER_ID_2, ORGANIZATION_ID_2, "org-2-user-1-display", "org-2-user-1");
// private static final String USER_EMAIL_ADDRESS = "user@umbrella.com";
// private static final String ROLE_ADMIN = "ROLE_ADMIN";
// private static final String ROLE_ORGANIZATION_ADMIN = "ROLE_ORG_ADMIN";
// private static final String ROLE_ORGANIZATION_USER = "ROLE_ORG_USER";
//
// private MockMvc mockMvc;
//
// @Autowired
// private ObjectMapper objectMapper;
// @Autowired
// private WebApplicationContext webApplicationContext;
// @Autowired
// private ReadServiceProvider readServiceProvider;
//
// @MockBean
// private DtoConverter dtoConverter;
// @MockBean
// private UsersService usersService;
// @MockBean
// private UsersReadService usersReadService;
//
// @BeforeEach
// public void setup() {
// Mockito.<ReadService<? extends Identifiable>>when(readServiceProvider.getService(User.class.getSimpleName()))
// .thenReturn(usersReadService);
// this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void retrievingGlobalUserList_WillDelegate() throws Exception {
// when(dtoConverter.convert(ORG_1_USER_1)).thenReturn(getUserResponse(ORG_1_USER_1));
// when(dtoConverter.convert(ORG_2_USER_1)).thenReturn(getUserResponse(ORG_2_USER_1));
// when(usersReadService.getUsers()).thenReturn(asList(ORG_1_USER_1, ORG_2_USER_1));
//
// mockMvc.perform(get("/api/users"))
// .andExpect(status().isOk())
// .andExpect(content().contentType(APPLICATION_JSON))
// .andExpect(jsonPath("$.[0].id", is(ORG_1_USER_1.getId().toString())))
// .andExpect(jsonPath("$.[1].id", is(ORG_2_USER_1.getId().toString())))
// .andExpect(jsonPath("$.[0].organizationId", is(ORGANIZATION_ID_1.toString())))
// .andExpect(jsonPath("$.[1].organizationId", is(ORGANIZATION_ID_2.toString())))
// .andExpect(jsonPath("$.[0].displayName", is(ORG_1_USER_1.getDisplayName())))
// .andExpect(jsonPath("$.[1].displayName", is(ORG_2_USER_1.getDisplayName())))
// .andExpect(jsonPath("$.[0].emailAddress", is(ORG_1_USER_1.getEmailAddress())))
// .andExpect(jsonPath("$.[1].emailAddress", is(ORG_2_USER_1.getEmailAddress())));
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void getUserDetails_WillRetrieveSingleUserDetails() throws Exception {
// when(dtoConverter.convert(ORG_2_USER_1)).thenReturn(getUserResponse(ORG_2_USER_1));
// when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);
//
// mockMvc.perform(get("/api/users/{userId}", ORG_2_USER_1.getId()).contentType(APPLICATION_JSON))
// .andExpect(status().isOk())
// .andExpect(content().contentType(APPLICATION_JSON))
// .andExpect(jsonPath("$.id", is(ORG_2_USER_1.getId().toString())))
// .andExpect(jsonPath("$.organizationId", is(ORGANIZATION_ID_2.toString())))
// .andExpect(jsonPath("$.displayName", is(ORG_2_USER_1.getDisplayName())))
// .andExpect(jsonPath("$.emailAddress", is(ORG_2_USER_1.getEmailAddress())));
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void updateUserDetailsWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
// when(dtoConverter.convert(ORG_2_USER_1)).thenReturn(getUserResponse(ORG_2_USER_1));
// when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);
//
// mockMvc.perform(put("/api/users/{userId}", ORG_2_USER_1.getId())
// .principal(() -> ADMIN_USER.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change"))))
// .andExpect(status().isOk());
//
// verify(usersService).updateUser(ADMIN_USER.getId(), ORG_2_USER_1.getId(), "email-change",
// "display-name-change");
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void addUserRolesWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
// var roles = Set.of(ADMIN);
// when(dtoConverter.convert(ORG_2_USER_1)).thenReturn(getUserResponse(ORG_2_USER_1));
// when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);
//
// mockMvc.perform(post("/api/users/{userId}/roles", ORG_2_USER_1.getId())
// .principal(() -> ADMIN_USER.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(roles)))
// .andExpect(status().isOk());
//
// verify(usersService).addUserRoles(ADMIN_USER.getId(), ORG_2_USER_1.getId(), roles);
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void removeUserRolesWillDelegate_WhenRequestingUserIsAdmin() throws Exception {
// var roles = Set.of(ADMIN);
// when(dtoConverter.convert(ORG_2_USER_1)).thenReturn(getUserResponse(ORG_2_USER_1));
// when(usersReadService.getById(ORG_2_USER_1.getId())).thenReturn(ORG_2_USER_1);
//
// mockMvc.perform(delete("/api/users/{userId}/roles", ORG_2_USER_1.getId())
// .principal(() -> ADMIN_USER.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(roles)))
// .andExpect(status().isOk());
//
// verify(usersService).removeUserRoles(ADMIN_USER.getId(), ORG_2_USER_1.getId(), roles);
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = { ROLE_ORGANIZATION_ADMIN })
// void updateUserDetailsWillDelegate_WhenRequestingUserIsAdminOfOrganization() throws Exception {
// var authUser = new User(randomUUID(), ORG_1_USER_1.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// var aUser = new User(randomUUID(), authUser.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// when(usersReadService.getById(aUser.getId())).thenReturn(aUser);
// mockMvc.perform(put("/api/users/{userId}", aUser.getId())
// .principal(() -> authUser.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change"))))
// .andExpect(status().isOk());
//
// verify(usersService).updateUser(authUser.getId(), aUser.getId(), "email-change",
// "display-name-change");
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = { ROLE_ORGANIZATION_USER })
// void updateUserDetailsWillDelegate_WhenRequestingUserIsTargetUser() throws Exception {
// var authUser = new User(randomUUID(), ORG_1_USER_1.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// when(usersReadService.getById(authUser.getId())).thenReturn(authUser);
//
// mockMvc.perform(put("/api/users/{userId}", authUser.getId())
// .principal(() -> authUser.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change"))))
// .andExpect(status().isOk());
//
// verify(usersService).updateUser(authUser.getId(), authUser.getId(), "email-change",
// "display-name-change");
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = { ROLE_ORGANIZATION_USER })
// void getUserById_WillDelegate() throws Exception {
// var authUser = new User(randomUUID(), ORG_1_USER_1.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// var targetUser = new User(randomUUID(), authUser.getOrganizationId(), "other", "other@umbrella.com");
// when(dtoConverter.convert(targetUser)).thenReturn(getUserResponse(targetUser));
// when(usersReadService.getById(targetUser.getId())).thenReturn(targetUser);
//
// mockMvc.perform(get("/api/users/{userId}", targetUser.getId()))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.displayName", is(targetUser.getDisplayName())))
// .andExpect(jsonPath("$.id", is(targetUser.getId().toString())))
// .andExpect(jsonPath("$.organizationId", is(targetUser.getOrganizationId().toString())));
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void getUserOfAnyOrganization_WillDelegateForAdmin() throws Exception {
// var targetUser = new User(randomUUID(), randomUUID(), "other", "other@umbrella.com");
// when(dtoConverter.convert(targetUser)).thenReturn(getUserResponse(targetUser));
// when(usersReadService.getById(targetUser.getId())).thenReturn(targetUser);
//
// mockMvc.perform(get("/api/users/{userId}", targetUser.getId()))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.displayName", is(targetUser.getDisplayName())))
// .andExpect(jsonPath("$.id", is(targetUser.getId().toString())))
// .andExpect(jsonPath("$.organizationId", is(targetUser.getOrganizationId().toString())));
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = { ROLE_ORGANIZATION_ADMIN })
// void updateUser_WillDelegate() throws Exception {
// var authUser = new User(randomUUID(), ORG_1_USER_1.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// UUID targetUserId = randomUUID();
// when(usersReadService.getById(targetUserId)).thenReturn(
// new User(targetUserId, authUser.getOrganizationId(), "", "some@umbrella.com"));
//
// mockMvc.perform(put("/api/users/{userId}", targetUserId)
// .principal(() -> authUser.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(new UpdateUserRequest("new", null))))
// .andExpect(status().isOk());
// }
//
// @Test
// @WithMockKeycloakAuth(authorities = ROLE_ADMIN)
// void deleteAndForgetUser_WillDelegate() throws Exception {
// var authUser = new User(randomUUID(), ORG_1_USER_1.getOrganizationId(), "user", USER_EMAIL_ADDRESS);
// UUID targetUserId = randomUUID();
//
// mockMvc.perform(post("/api/users/{userId}/forget", targetUserId)
// .principal(() -> authUser.getId().toString())
// .contentType(APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(new DeleteAndForgetUserRequest("Submitted GDPR request"))))
// .andExpect(status().isOk());
//
// verify(usersService).deleteAndForget(authUser.getId(), targetUserId, "Submitted GDPR request");
// }
//
// private static UserResponse getUserResponse(User user) {
// return new UserResponse(user.getId(),
// user.getOrganizationId(),
// user.getDisplayName(),
// user.getEmailAddress(),
// user.isDisabled());
// }
// }
