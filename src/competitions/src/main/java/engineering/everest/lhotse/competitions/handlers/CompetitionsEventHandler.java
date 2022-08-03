package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEnteredInCompetitionEvent;
import engineering.everest.lhotse.competitions.domain.queries.CompetitionWithEntriesQuery;
import engineering.everest.lhotse.competitions.persistence.CompetitionEntriesRepository;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class CompetitionsEventHandler {

    private final QueryUpdateEmitter queryUpdateEmitter;
    private final CompetitionsReadService competitionsReadService;
    private final CompetitionsRepository competitionsRepository;
    private final CompetitionEntriesRepository competitionEntriesRepository;

    public CompetitionsEventHandler(QueryUpdateEmitter queryUpdateEmitter,
                                    CompetitionsReadService competitionsReadService,
                                    CompetitionsRepository competitionsRepository,
                                    CompetitionEntriesRepository competitionEntriesRepository) {
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.competitionsReadService = competitionsReadService;
        this.competitionsRepository = competitionsRepository;
        this.competitionEntriesRepository = competitionEntriesRepository;
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

    @EventHandler
    void on(PhotoEnteredInCompetitionEvent event, @Timestamp Instant entryTimestamp) {
        LOGGER.info("Photo {} entered into competition {} by user {}", event.getPhotoId(), event.getCompetitionId(),
            event.getSubmittedByUserId());
        competitionEntriesRepository.createCompetitionEntry(event.getCompetitionId(), event.getPhotoId(),
            event.getSubmittedByUserId(), entryTimestamp);

        queryUpdateEmitter.emit(CompetitionWithEntriesQuery.class,
            filter -> event.getCompetitionId().equals(filter.getCompetitionId()),
            competitionsReadService.getCompetitionWithEntries(event.getCompetitionId()));
    }
}
