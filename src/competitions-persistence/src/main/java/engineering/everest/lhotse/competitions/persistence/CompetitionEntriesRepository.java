package engineering.everest.lhotse.competitions.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompetitionEntriesRepository extends JpaRepository<PersistableCompetitionEntry, CompetitionEntryId> {

    default void createCompetitionEntry(UUID competitionId, UUID photoId, UUID submittedByUserId, Instant entryTimestamp) {
        save(new PersistableCompetitionEntry(competitionId, photoId, submittedByUserId, entryTimestamp, 0));
    }

    List<PersistableCompetitionEntry> findAllByCompetitionId(UUID competitionId, Sort sort);
}
