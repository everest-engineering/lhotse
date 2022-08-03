package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;

import java.util.List;
import java.util.UUID;

public interface CompetitionsReadService {

    List<Competition> getAllCompetitionsOrderedByDescVotingEndsTimestamp();

    CompetitionWithEntries getCompetitionWithEntries(UUID competitionId);
}
