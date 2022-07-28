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
}
