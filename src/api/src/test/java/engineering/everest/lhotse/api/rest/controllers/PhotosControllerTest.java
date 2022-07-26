package engineering.everest.lhotse.api.rest.controllers;

import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import engineering.everest.lhotse.api.config.TestApiConfig;
import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.lhotse.photos.services.PhotosService;
import engineering.everest.starterkit.filestorage.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { PhotosController.class })
@ContextConfiguration(classes = { TestApiConfig.class, PhotosController.class })
class PhotosControllerTest {

    private static final String ROLE_REGISTERED_USER = "ROLE_REGISTERED_USER";
    private static final byte[] PHOTO_FILE_CONTENTS = "pretend this is a photo".getBytes();
    private static final UUID USER_ID = randomUUID();
    private static final Photo PHOTO_1 = new Photo(randomUUID(), USER_ID, randomUUID(), "photo1.png", Instant.ofEpochSecond(1234));
    private static final Photo PHOTO_2 = new Photo(randomUUID(), USER_ID, randomUUID(), "photo2.png", Instant.ofEpochSecond(5678));

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private PhotosReadService photosReadService;
    @MockBean
    private PhotosService photosService;
    @MockBean
    private FileService fileService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_REGISTERED_USER)
    void uploadPhotosWillPersistAndRegisterUpload() throws Exception {
        var persistedFileId = randomUUID();
        when(fileService.transferToPermanentStore(eq("photo1.png"), eq((long) PHOTO_FILE_CONTENTS.length), any(InputStream.class)))
            .thenReturn(persistedFileId);

        mockMvc.perform(multipart("/api/photos")
            .file(new MockMultipartFile("file", "photo1.png", IMAGE_PNG_VALUE, PHOTO_FILE_CONTENTS))
            .contentType(MULTIPART_FORM_DATA)
            .principal(USER_ID::toString))
            .andExpect(status().isCreated());

        verify(fileService).transferToPermanentStore(
            eq("photo1.png"),
            eq((long) PHOTO_FILE_CONTENTS.length),
            any(InputStream.class));
        verify(photosService).registerUploadedPhoto(USER_ID, persistedFileId, "photo1.png");
    }

    @Test
    @WithMockKeycloakAuth(authorities = ROLE_REGISTERED_USER)
    void getListOfPhotosForAuthenticatedUserWillDelegate() throws Exception {
        when(photosReadService.getAllPhotosForUser(eq(USER_ID), any(Pageable.class)))
            .thenReturn(List.of(PHOTO_1, PHOTO_2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/photos")
            .principal(USER_ID::toString))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(PHOTO_1.getId().toString()))
            .andExpect(jsonPath("$[0].filename").value(PHOTO_1.getFilename()))
            .andExpect(jsonPath("$[0].uploadTimestamp").value(PHOTO_1.getUploadTimestamp().toString()))
            .andExpect(jsonPath("$[1].id").value(PHOTO_2.getId().toString()))
            .andExpect(jsonPath("$[1].filename").value(PHOTO_2.getFilename()))
            .andExpect(jsonPath("$[1].uploadTimestamp").value(PHOTO_2.getUploadTimestamp().toString()));
    }
}
