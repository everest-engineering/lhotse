package engineering.everest.lhotse.competitions.services;

import java.time.Instant;
import java.util.UUID;

public interface CompetitionsService {

    UUID createCompetition(UUID requestingUserId,
                           String description,
                           Instant submissionsOpenTimestamp,
                           Instant submissionsCloseTimestamp,
                           Instant votingEndsTimestamp,
                           int maxEntriesPerUser);

    void submitPhoto(UUID requestingUserId, UUID competitionId, UUID photoId, String submissionNotes);

    void voteForPhoto(UUID requestingUserId, UUID competitionId, UUID photoId);
}
