package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.persistence.CompetitionEntriesRepository;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import engineering.everest.lhotse.competitions.persistence.PersistableCompetition;
import engineering.everest.lhotse.competitions.persistence.PersistableCompetitionEntry;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
public class DefaultCompetitionsReadService implements CompetitionsReadService {

    private final CompetitionsRepository competitionsRepository;
    private final CompetitionEntriesRepository competitionEntriesRepository;

    public DefaultCompetitionsReadService(CompetitionsRepository competitionsRepository,
                                          CompetitionEntriesRepository competitionEntriesRepository) {
        this.competitionsRepository = competitionsRepository;
        this.competitionEntriesRepository = competitionEntriesRepository;
    }

    @Override
    public List<Competition> getAllCompetitionsOrderedByDescVotingEndsTimestamp() {
        return competitionsRepository.findAll(Sort.by(DESC, "votingEndsTimestamp")).stream()
            .map(PersistableCompetition::toDomain)
            .toList();
    }

    @Override
    public CompetitionWithEntries getCompetitionWithEntries(UUID competitionId) {
        var entries = competitionEntriesRepository
            .findAllByCompetitionId(competitionId, Sort.by(ASC, "entryTimestamp")).stream()
            .map(PersistableCompetitionEntry::toDomain)
            .toList();
        return new CompetitionWithEntries(competitionsRepository.findById(competitionId).orElseThrow().toDomain(), entries);
    }
}
