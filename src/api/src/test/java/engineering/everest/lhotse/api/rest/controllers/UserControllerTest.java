package engineering.everest.lhotse.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import engineering.everest.starterkit.filestorage.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { UserController.class })
@ContextConfiguration(classes = { TestApiConfig.class, UserController.class })
class UserControllerTest {
    private static final byte[] PROFILE_PHOTO_FILE_CONTENTS = "profile-photo-file-contents".getBytes();
    private static final byte[] PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS = "profile-photo-thumbnail-file-contents".getBytes();
    private static final UUID USER_ID_1 = randomUUID();
    private static final UUID ORGANIZATION_ID_1 = randomUUID();
    private static final User ORG_1_USER_1 = new User(USER_ID_1, ORGANIZATION_ID_1, "org-1-user-1", "org-1-user-1-display");

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private DtoConverter dtoConverter;
    @MockBean
    private UsersService usersService;
    @MockBean
    private UsersReadService usersReadService;
    @MockBean
    private FileService fileService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void willGetUserInfo() throws Exception {
        when(dtoConverter.convert(ORG_1_USER_1)).thenReturn(getUserResponse());
        when(usersReadService.getById(ORG_1_USER_1.getId())).thenReturn(ORG_1_USER_1);

        mockMvc.perform(get("/api/user").contentType(APPLICATION_JSON)
            .principal(() -> ORG_1_USER_1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(ORG_1_USER_1.getId().toString())));
    }

    @Test
    void willUpdateUserInfo() throws Exception {
        mockMvc.perform(put("/api/user")
            .principal(USER_ID_1::toString)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change"))))
            .andExpect(status().isOk());

        verify(usersService).updateUser(USER_ID_1, USER_ID_1, "email-change",
            "display-name-change");
    }

    @Test
    void uploadProfilePhoto_WillPersistUploadedFile() throws Exception {
        var persistedFileId = randomUUID();
        when(fileService.transferToPermanentStore(eq(
            "profile-photo-file-name"),
            eq(PROFILE_PHOTO_FILE_CONTENTS.length),
            any(InputStream.class))).thenReturn(persistedFileId);

        mockMvc.perform(multipart("/api/user/profile-photo")
            .file(new MockMultipartFile("file", "profile-photo-file-name", IMAGE_JPEG_VALUE, PROFILE_PHOTO_FILE_CONTENTS))
            .contentType(MULTIPART_FORM_DATA)
            .principal(USER_ID_1::toString))
            .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
            .andExpect(status().isOk());

        verify(fileService).transferToPermanentStore(
            eq("profile-photo-file-name"),
            eq((long) PROFILE_PHOTO_FILE_CONTENTS.length),
            any(InputStream.class));
    }

    @Test
    void streamProfilePhoto_WillReturnProfilePhoto() throws Exception {
        InputStream profilePhotoInputStream = mock(InputStream.class);
        when(profilePhotoInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        when(usersReadService.getProfilePhotoStream(USER_ID_1)).thenReturn(profilePhotoInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo")
            .principal(USER_ID_1::toString))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(response))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(PROFILE_PHOTO_FILE_CONTENTS));
    }

    @Test
    void streamProfilePhotoThumbnail_WillReturnProfilePhotoThumbnail() throws Exception {
        InputStream profilePhotoThumbnailInputStream = mock(InputStream.class);
        when(profilePhotoThumbnailInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        when(usersReadService.getProfilePhotoThumbnailStream(USER_ID_1, 100, 100)).thenReturn(profilePhotoThumbnailInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo/thumbnail?width=100&height=100")
            .principal(USER_ID_1::toString))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(response))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS));
    }

    @Test
    void streamProfilePhotoThumbnail_WillFail_WhenQueryParamsMissing() throws Exception {
        mockMvc.perform(get("/api/user/profile-photo/thumbnail"))
            .andExpect(status().isBadRequest());
    }

    private static UserResponse getUserResponse() {
        return new UserResponse(UserControllerTest.ORG_1_USER_1.getId(),
            UserControllerTest.ORG_1_USER_1.getOrganizationId(),
            UserControllerTest.ORG_1_USER_1.getUsername(),
            UserControllerTest.ORG_1_USER_1.getDisplayName(),
            UserControllerTest.ORG_1_USER_1.getEmail(),
            UserControllerTest.ORG_1_USER_1.isDisabled());
    }
}
