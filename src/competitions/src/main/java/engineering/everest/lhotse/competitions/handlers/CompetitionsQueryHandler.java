package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.domain.queries.CompetitionWithEntriesQuery;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompetitionsQueryHandler {

    private final CompetitionsReadService competitionsReadService;

    public CompetitionsQueryHandler(CompetitionsReadService competitionsReadService) {
        this.competitionsReadService = competitionsReadService;
    }

    @QueryHandler
    public CompetitionWithEntries handle(CompetitionWithEntriesQuery query) {
        return competitionsReadService.getCompetitionWithEntries(query.getCompetitionId());
    }
}
