package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.common.AuthenticationFacade;
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
    private final AuthenticationFacade authFacade;

    public DefaultCompetitionsService(CommandGateway commandGateway,
                                      RandomFieldsGenerator randomFieldsGenerator,
                                      PhotosReadService photosReadService, AuthenticationFacade authFacade) {
        this.commandGateway = commandGateway;
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.photosReadService = photosReadService;
        this.authFacade = authFacade;
    }

    @Override
    public UUID createCompetition(String description,
                                  Instant submissionsOpenTimestamp,
                                  Instant submissionsCloseTimestamp,
                                  Instant votingEndsTimestamp,
                                  int maxEntriesPerUser) {
        var competitionId = randomFieldsGenerator.genRandomUUID();
        var requestingUserId = authFacade.getRequestingUserId();
        commandGateway.sendAndWait(new CreateCompetitionCommand(UUID.fromString(requestingUserId), competitionId, description,
            submissionsOpenTimestamp, submissionsCloseTimestamp, votingEndsTimestamp, maxEntriesPerUser));
        return competitionId;
    }

    @Override
    public void submitPhoto(UUID requestingUserId, UUID competitionId, UUID photoId, String submissionNotes) {
        var owningUserId = photosReadService.getPhoto(photoId).getOwningUserId();
        commandGateway.sendAndWait(new EnterPhotoInCompetitionCommand(competitionId, photoId, requestingUserId,
            owningUserId, submissionNotes));
    }

    @Override
    public void voteForPhoto(UUID requestingUserId, UUID competitionId, UUID photoId) {
        commandGateway.sendAndWait(new VoteForPhotoCommand(competitionId, photoId, requestingUserId));
    }
}
