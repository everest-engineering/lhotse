package engineering.everest.lhotse.competitions.domain.commands;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompetitionCommand implements Serializable {
    private UUID requestingUserId;
    @TargetAggregateIdentifier
    private UUID competitionId;
    private String description;
    @NotNull
    private Instant submissionsOpenTimestamp;
    @NotNull
    private Instant submissionsCloseTimestamp;
    @NotNull
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;
}
