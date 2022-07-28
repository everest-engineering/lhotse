package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CompetitionsEventHandler {

    private final CompetitionsRepository competitionsRepository;

    public CompetitionsEventHandler(CompetitionsRepository competitionsRepository) {
        this.competitionsRepository = competitionsRepository;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", CompetitionsRepository.class.getSimpleName());
        competitionsRepository.deleteAll();
    }

    @EventHandler
    void on(CompetitionCreatedEvent event) {
        LOGGER.info("Competition {} created", event.getCompetitionId());
        competitionsRepository.createCompetition(event.getCompetitionId(), event.getDescription(), event.getSubmissionsOpenTimestamp(),
            event.getSubmissionsCloseTimestamp(), event.getVotingEndsTimestamp(), event.getMaxEntriesPerUser());
    }
}
