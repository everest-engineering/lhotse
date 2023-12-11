package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.common.AuthenticatedUser;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.EnterPhotoInCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultCompetitionsService implements CompetitionsService {

    private final CommandGateway commandGateway;
    private final RandomFieldsGenerator randomFieldsGenerator;
    private final PhotosReadService photosReadService;
    private final AuthenticatedUser authenticatedUser;

    public DefaultCompetitionsService(CommandGateway commandGateway,
                                      RandomFieldsGenerator randomFieldsGenerator,
                                      PhotosReadService photosReadService,
                                      AuthenticatedUser authenticatedUser) {
        this.commandGateway = commandGateway;
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.photosReadService = photosReadService;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public UUID createCompetition(String description,
                                  Instant submissionsOpenTimestamp,
                                  Instant submissionsCloseTimestamp,
                                  Instant votingEndsTimestamp,
                                  int maxEntriesPerUser) {
        var competitionId = randomFieldsGenerator.genRandomUUID();
        var requestingUserId = authenticatedUser.getUserId();
        commandGateway.sendAndWait(new CreateCompetitionCommand(requestingUserId, competitionId, description,
            submissionsOpenTimestamp, submissionsCloseTimestamp, votingEndsTimestamp, maxEntriesPerUser));
        return competitionId;
    }

    @Override
    public void submitPhoto(UUID competitionId, UUID photoId, String submissionNotes) {
        var owningUserId = photosReadService.getPhoto(photoId).getOwningUserId();
        var requestingUserId = authenticatedUser.getUserId();
        commandGateway.sendAndWait(new EnterPhotoInCompetitionCommand(competitionId, photoId, requestingUserId,
            owningUserId, submissionNotes));
    }

    @Override
    public void voteForPhoto(UUID competitionId, UUID photoId) {
        var requestingUserId = authenticatedUser.getUserId();
        commandGateway.sendAndWait(new VoteForPhotoCommand(competitionId, photoId, requestingUserId));
    }
}
