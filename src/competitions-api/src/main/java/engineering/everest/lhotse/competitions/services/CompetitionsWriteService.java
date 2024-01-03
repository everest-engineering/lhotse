package engineering.everest.lhotse.competitions.services;

import java.time.Instant;
import java.util.UUID;

public interface CompetitionsWriteService {
    void createCompetition(UUID id,
                           String description,
                           Instant submissionsOpenTimestamp,
                           Instant submissionsCloseTimestamp,
                           Instant votingEndsTimestamp,
                           int maxEntriesPerUser);

    void createCompetitionEntry(UUID competitionId, UUID photoId, UUID submittedByUserId, Instant entryTimestamp);

    void incrementVotesReceived(UUID competitionId, UUID photoId);

    void setCompetitionWinner(UUID competitionId, UUID photoId);

    void deleteAll();
}
