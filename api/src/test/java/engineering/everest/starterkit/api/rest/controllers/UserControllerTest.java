package engineering.everest.starterkit.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.starterkit.api.config.TestApiConfig;
import engineering.everest.starterkit.api.helpers.AuthContextExtension;
import engineering.everest.starterkit.api.helpers.MockAuthenticationContextProvider;
import engineering.everest.starterkit.api.rest.requests.UpdateUserRequest;
import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.axon.filehandling.FileService;
import engineering.everest.starterkit.users.services.UsersReadService;
import engineering.everest.starterkit.users.services.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TestApiConfig.class, UserController.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class, AuthContextExtension.class})
class UserControllerTest {

    private static final byte[] PROFILE_PHOTO_FILE_CONTENTS = "profile-photo-file-contents".getBytes();
    private static final byte[] PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS = "profile-photo-thumbnail-file-contents".getBytes();
    private static final String ROLE_ORGANIZATION_USER = "ORG_USER";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UsersService usersService;
    @MockBean
    private FileService fileService;
    @MockBean
    private UsersReadService usersReadService;

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void willGetUserInfo() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(authUser.getId().toString())));
    }

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void willUpdateUserInfo() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();
        mockMvc.perform(put("/api/user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateUserRequest("display-name-change", "email-change", "password-change"))))
                .andExpect(status().isOk());

        verify(usersService).updateUser(authUser.getId(), authUser.getId(), "email-change",
                "display-name-change", "password-change");
    }

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void uploadProfilePhoto_WillPersistUploadedFile() throws Exception {
        var persistedFileId = randomUUID();
        when(fileService.transferToPermanentStore(eq("profile-photo-file-name"), any(InputStream.class))).thenReturn(persistedFileId);

        mockMvc.perform(multipart("/api/user/profile-photo")
                .file(new MockMultipartFile("file", "profile-photo-file-name", IMAGE_JPEG_VALUE, PROFILE_PHOTO_FILE_CONTENTS)))
                .andExpect(status().isOk());

        verify(fileService).transferToPermanentStore(eq("profile-photo-file-name"), any(InputStream.class));
    }

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void streamProfilePhoto_WillReturnProfilePhoto() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();

        InputStream profilePhotoInputStream = mock(InputStream.class);
        when(profilePhotoInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        when(usersReadService.getProfilePhotoStream(authUser.getId())).thenReturn(profilePhotoInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(response))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(PROFILE_PHOTO_FILE_CONTENTS));
    }

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void streamProfilePhotoThumbnail_WillReturnProfilePhotoThumbnail() throws Exception {
        User authUser = MockAuthenticationContextProvider.getAuthUser();


        InputStream profilePhotoThumbnailInputStream = mock(InputStream.class);
        when(profilePhotoThumbnailInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        when(usersReadService.getProfilePhotoThumbnailStream(authUser.getId(), 100, 100)).thenReturn(profilePhotoThumbnailInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo/thumbnail?width=100&height=100"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(response))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS));
    }

    @Test
    @WithMockUser(username = "user@umbrella.com", roles = ROLE_ORGANIZATION_USER)
    void streamProfilePhotoThumbnail_WillFail_WhenQueryParamsMissing() throws Exception {
        mockMvc.perform(get("/api/user/profile-photo/thumbnail"))
                .andExpect(status().isBadRequest());
    }
}
