package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.competitions.domain.commands.CountVotesAndDeclareOutcomeCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionVotingPeriodEndedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.ScheduleToken;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;

import java.io.Serializable;

@Saga
@Revision("0")
@Slf4j
public class CompetitionVotingCloseoutSaga implements Serializable {

    private ScheduleToken votingPeriodEndedEventScheduleToken;

    @StartSaga
    @SagaEventHandler(associationProperty = "competitionId")
    void on(CompetitionCreatedEvent event, EventScheduler eventScheduler) {
        LOGGER.debug("Competition {} created, scheduling competition ending event", event.getCompetitionId());

        votingPeriodEndedEventScheduleToken = eventScheduler.schedule(event.getVotingEndsTimestamp(),
            new CompetitionVotingPeriodEndedEvent(event.getCompetitionId(), event.getVotingEndsTimestamp()));
    }

    @SagaEventHandler(associationProperty = "competitionId")
    void on(CompetitionVotingPeriodEndedEvent event, CommandGateway commandGateway) {
        LOGGER.debug("Competition {} voting period ended", event.getCompetitionId());
        commandGateway.send(new CountVotesAndDeclareOutcomeCommand(event.getCompetitionId()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "competitionId")
    void on(CompetitionEndedEvent event) {
        LOGGER.debug("Competition {} has ended", event.getCompetitionId());
    }
}
