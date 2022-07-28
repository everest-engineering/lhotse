package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultCompetitionsService implements CompetitionsService {

    private final CommandGateway commandGateway;
    private final RandomFieldsGenerator randomFieldsGenerator;

    public DefaultCompetitionsService(CommandGateway commandGateway, RandomFieldsGenerator randomFieldsGenerator) {
        this.commandGateway = commandGateway;
        this.randomFieldsGenerator = randomFieldsGenerator;
    }

    @Override
    public UUID createCompetition(UUID requestingUserId,
                                  String description,
                                  Instant submissionsOpenTimestamp,
                                  Instant submissionsCloseTimestamp,
                                  Instant votingEndsTimestamp,
                                  int maxEntriesPerUser) {
        var competitionId = randomFieldsGenerator.genRandomUUID();
        commandGateway.sendAndWait(new CreateCompetitionCommand(requestingUserId, competitionId, description,
            submissionsOpenTimestamp, submissionsCloseTimestamp, votingEndsTimestamp, maxEntriesPerUser));
        return competitionId;
    }
}
