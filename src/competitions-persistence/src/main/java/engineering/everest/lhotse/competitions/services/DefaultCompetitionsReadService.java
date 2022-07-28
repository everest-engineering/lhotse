package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultCompetitionsReadService implements CompetitionsReadService {

    private final CompetitionsRepository competitionsRepository;

    public DefaultCompetitionsReadService(CompetitionsRepository competitionsRepository) {
        this.competitionsRepository = competitionsRepository;
    }
}
