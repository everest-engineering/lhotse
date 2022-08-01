package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.domain.commands.DeletePhotoForDeletedUserCommand;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Pageable.unpaged;

@ExtendWith(MockitoExtension.class)
class UserDeletedSagaTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final UUID PHOTO_ID_1 = randomUUID();
    private static final UUID PHOTO_ID_2 = randomUUID();
    private static final UUID BACKING_FILE_ID_1 = randomUUID();
    private static final UUID BACKING_FILE_ID_2 = randomUUID();

    private SagaTestFixture<UserDeletedSaga> testFixture;

    @Autowired
    private CommandGateway commandGateway;
    @Mock
    private PhotosReadService photosReadService;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(UserDeletedSaga.class);
        testFixture.registerResource(photosReadService);
        // testFixture.registerResource(commandGateway);
    }

    @Test
    void userDeletedAndForgotten_WillDispatchPhotoDeletionCommands() {
        when(photosReadService.getAllPhotos(USER_ID, unpaged()))
            .thenReturn(List.of(
                new Photo(PHOTO_ID_1, USER_ID, BACKING_FILE_ID_1, "photo1.png", Instant.now()),
                new Photo(PHOTO_ID_2, USER_ID, BACKING_FILE_ID_2, "photo2.png", Instant.now())));

        testFixture.givenNoPriorActivity()
            .whenAggregate(USER_ID.toString())
            .publishes(new UserDeletedAndForgottenEvent(USER_ID, ADMIN_ID, "GDPR request"))
            .expectDispatchedCommands(
                new DeletePhotoForDeletedUserCommand(PHOTO_ID_1, USER_ID),
                new DeletePhotoForDeletedUserCommand(PHOTO_ID_2, USER_ID));
    }
}
