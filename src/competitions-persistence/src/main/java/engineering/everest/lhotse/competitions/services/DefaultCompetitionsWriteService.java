package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.persistence.CompetitionEntriesRepository;
import engineering.everest.lhotse.competitions.persistence.CompetitionEntryId;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultCompetitionsWriteService implements CompetitionsWriteService {
    private final CompetitionsRepository competitionsRepository;
    private final CompetitionEntriesRepository competitionEntriesRepository;

    public DefaultCompetitionsWriteService(CompetitionsRepository competitionsRepository,
                                           CompetitionEntriesRepository competitionEntriesRepository) {
        this.competitionsRepository = competitionsRepository;
        this.competitionEntriesRepository = competitionEntriesRepository;
    }

    @Override
    public void createCompetition(UUID id,
                                  String description,
                                  Instant submissionsOpenTimestamp,
                                  Instant submissionsCloseTimestamp,
                                  Instant votingEndsTimestamp,
                                  int maxEntriesPerUser) {
        competitionsRepository.createCompetition(id, description, submissionsOpenTimestamp,
            submissionsCloseTimestamp, votingEndsTimestamp, maxEntriesPerUser);
    }

    @Override
    public void createCompetitionEntry(UUID competitionId, UUID photoId, UUID submittedByUserId, Instant entryTimestamp) {
        competitionEntriesRepository.createCompetitionEntry(competitionId, photoId, submittedByUserId, entryTimestamp);
    }

    @Override
    public void incrementVotesReceived(UUID competitionId, UUID photoId) {
        var competitionEntry = competitionEntriesRepository.findById(new CompetitionEntryId(competitionId, photoId)).orElseThrow();

        competitionEntry.setVotesReceived(competitionEntry.getVotesReceived() + 1);
    }

    @Override
    public void setCompetitionWinner(UUID competitionId, UUID photoId) {
        var competitionEntry = competitionEntriesRepository.findById(new CompetitionEntryId(competitionId, photoId)).orElseThrow();

        competitionEntry.setWinner(true);
    }

    @Override
    public void deleteAll() {
        competitionsRepository.deleteAll();
    }
}
