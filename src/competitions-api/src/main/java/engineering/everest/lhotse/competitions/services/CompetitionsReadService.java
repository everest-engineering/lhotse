package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.domain.Competition;

import java.util.List;

public interface CompetitionsReadService {

    List<Competition> getAllCompetitionsOrderedByDescVotingEndsTimestamp();
}
