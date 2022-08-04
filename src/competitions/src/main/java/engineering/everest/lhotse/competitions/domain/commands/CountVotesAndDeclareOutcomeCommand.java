package engineering.everest.lhotse.competitions.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountVotesAndDeclareOutcomeCommand {
    @TargetAggregateIdentifier
    private UUID competitionId;
}
