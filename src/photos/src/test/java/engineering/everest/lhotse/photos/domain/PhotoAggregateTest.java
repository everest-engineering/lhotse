package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validators.FileStatusValidator;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PhotoAggregateTest {

    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID PHOTO_BACKING_FILE_ID = randomUUID();
    private static final String PHOTO_FILENAME = "holiday photo.png";

    private FixtureConfiguration<PhotoAggregate> testFixture;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private FileStatusValidator fileStatusValidator;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();

        testFixture = new AggregateTestFixture<>(PhotoAggregate.class)
            .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor(
                fileStatusValidator))
            .registerInjectableResource(axonCommandExecutionExceptionFactory);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var aggregateAnnotation = PhotoAggregate.class.getAnnotation(Aggregate.class);
        assertEquals("photoAggregateSnapshotTriggerDefinition", aggregateAnnotation.snapshotTriggerDefinition());
    }

    @Test
    void emits_WhenPhotoRegistered() {
        testFixture.givenNoPriorActivity()
            .when(new RegisterUploadedPhotoCommand(PHOTO_ID, USER_ID, PHOTO_BACKING_FILE_ID, PHOTO_FILENAME))
            .expectEvents(new PhotoUploadedEvent(PHOTO_ID, USER_ID, PHOTO_BACKING_FILE_ID, PHOTO_FILENAME));
    }

    @Test
    void rejects_WhenRegisteredPhotoHasNoBackingFile() {
        var command = new RegisterUploadedPhotoCommand(PHOTO_ID, USER_ID, PHOTO_BACKING_FILE_ID, PHOTO_FILENAME);
        doThrow(NoSuchElementException.class).when(fileStatusValidator).validate(command);

        testFixture.givenNoPriorActivity()
            .when(command)
            .expectNoEvents()
            .expectException(NoSuchElementException.class);
    }
}
