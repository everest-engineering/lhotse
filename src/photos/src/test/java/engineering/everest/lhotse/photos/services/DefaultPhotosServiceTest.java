package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.common.AuthenticatedUser;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPhotosServiceTest {

    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID BACKING_FILE_ID = randomUUID();
    private static final String PHOTO_FILENAME = "my holiday snap.png";

    private DefaultPhotosService defaultPhotosService;

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        defaultPhotosService = new DefaultPhotosService(commandGateway, randomFieldsGenerator, authenticatedUser);
    }

    @Test
    void willDispatch_WhenUploadedPhotoRegistered() {
        when(randomFieldsGenerator.genRandomUUID()).thenReturn(PHOTO_ID);
        when(authenticatedUser.getUserId()).thenReturn(USER_ID);
        defaultPhotosService.registerUploadedPhoto(BACKING_FILE_ID, PHOTO_FILENAME);

        verify(commandGateway).sendAndWait(new RegisterUploadedPhotoCommand(PHOTO_ID, USER_ID, BACKING_FILE_ID, PHOTO_FILENAME));
    }
}
