package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.domain.Competition;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import engineering.everest.lhotse.competitions.persistence.PersistableCompetition;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
public class DefaultCompetitionsReadService implements CompetitionsReadService {

    private final CompetitionsRepository competitionsRepository;

    public DefaultCompetitionsReadService(CompetitionsRepository competitionsRepository) {
        this.competitionsRepository = competitionsRepository;
    }

    @Override
    public List<Competition> getAllCompetitionsOrderedByDescVotingEndsTimestamp() {
        return competitionsRepository.findAll(Sort.by(DESC, "votingEndsTimestamp")).stream()
            .map(PersistableCompetition::toDomain)
            .collect(toList());
    }
}
