package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.common.AuthenticatedUser;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.EnterPhotoInCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCompetitionsServiceTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);

    private DefaultCompetitionsService defaultCompetitionsService;

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private PhotosReadService photosReadService;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        defaultCompetitionsService = new DefaultCompetitionsService(commandGateway, randomFieldsGenerator,
            photosReadService, authenticatedUser);
    }

    @Test
    void createCompetition_WillDispatch() {
        when(randomFieldsGenerator.genRandomUUID()).thenReturn(COMPETITION_ID);
        when(authenticatedUser.getUserId()).thenReturn(USER_ID);

        defaultCompetitionsService.createCompetition("description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);

        verify(commandGateway).sendAndWait(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "description",
            SUBMISSIONS_OPEN_TIMESTAMP, SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2));
    }

    @Test
    void submitPhoto_WillDispatch() {
        var ownerUserId = randomUUID();
        when(photosReadService.getPhoto(PHOTO_ID))
            .thenReturn(new Photo(PHOTO_ID, ownerUserId, randomUUID(), "file name", Instant.ofEpochMilli(10)));
        when(authenticatedUser.getUserId()).thenReturn(USER_ID);

        defaultCompetitionsService.submitPhoto(COMPETITION_ID, PHOTO_ID, "submission notes");

        verify(commandGateway).sendAndWait(new EnterPhotoInCompetitionCommand(COMPETITION_ID, PHOTO_ID, USER_ID,
            ownerUserId, "submission notes"));
    }

    @Test
    void voteForPhoto_WillDispatch() {
        defaultCompetitionsService.voteForPhoto(USER_ID, COMPETITION_ID, PHOTO_ID);

        verify(commandGateway).sendAndWait(new VoteForPhotoCommand(COMPETITION_ID, PHOTO_ID, USER_ID));
    }
}
