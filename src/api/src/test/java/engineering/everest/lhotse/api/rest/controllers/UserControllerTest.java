package engineering.everest.lhotse.api.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Disabled;

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

import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = { TestApiConfig.class, UserController.class })
@Import({ ServletKeycloakAuthUnitTestingSupport.UnitTestConfig.class })
@AutoConfigureMockMvc
@ActiveProfiles("keycloak")
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
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
    @WithMockKeycloakAuth
    void willGetUserInfo() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    @WithMockKeycloakAuth
    void uploadProfilePhoto_WillPersistUploadedFile() throws Exception {
        var persistedFileId = randomUUID();
        when(fileService.transferToPermanentStore(eq(
                "profile-photo-file-name"),
                eq(PROFILE_PHOTO_FILE_CONTENTS.length),
                any(InputStream.class))).thenReturn(persistedFileId);

        mockMvc.perform(multipart("/api/user/profile-photo")
                .file(new MockMultipartFile("file", "profile-photo-file-name", IMAGE_JPEG_VALUE, PROFILE_PHOTO_FILE_CONTENTS)))
                .andExpect(status().isOk());

        verify(fileService).transferToPermanentStore(
                eq("profile-photo-file-name"),
                eq((long)PROFILE_PHOTO_FILE_CONTENTS.length),
                any(InputStream.class));
    }

    @Test
    @Disabled
    @WithMockKeycloakAuth
    void streamProfilePhoto_WillReturnProfilePhoto() throws Exception {
        InputStream profilePhotoInputStream = mock(InputStream.class);
        when(profilePhotoInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        // when(usersReadService.getProfilePhotoStream(authUser.getId())).thenReturn(profilePhotoInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(response))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(PROFILE_PHOTO_FILE_CONTENTS));
    }

    @Test
    @Disabled
    @WithMockKeycloakAuth
    void streamProfilePhotoThumbnail_WillReturnProfilePhotoThumbnail() throws Exception {
        InputStream profilePhotoThumbnailInputStream = mock(InputStream.class);
        when(profilePhotoThumbnailInputStream.transferTo(any(OutputStream.class))).thenAnswer(invocation -> {
            var outputStream = invocation.getArgument(0);
            new ByteArrayInputStream(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS).transferTo((OutputStream) outputStream);
            return null;
        });
        // when(usersReadService.getProfilePhotoThumbnailStream(authUser.getId(), 100, 100)).thenReturn(profilePhotoThumbnailInputStream);

        var response = mockMvc.perform(get("/api/user/profile-photo/thumbnail?width=100&height=100"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(response))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(PROFILE_PHOTO_THUMBNAIL_FILE_CONTENTS));
    }

    @Test
    @WithMockKeycloakAuth
    void streamProfilePhotoThumbnail_WillFail_WhenQueryParamsMissing() throws Exception {
        mockMvc.perform(get("/api/user/profile-photo/thumbnail"))
                .andExpect(status().isBadRequest());
    }

}
