package engineering.everest.lhotse.competitions.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface CompetitionsRepository extends JpaRepository<PersistableCompetition, UUID> {

    default void createCompetition(UUID id,
                                   String description,
                                   Instant submissionsOpenTimestamp,
                                   Instant submissionsCloseTimestamp,
                                   Instant votingEndsTimestamp,
                                   int maxEntriesPerUser) {
        save(new PersistableCompetition(id, description, submissionsOpenTimestamp, submissionsCloseTimestamp, votingEndsTimestamp,
            maxEntriesPerUser));
    }
}
