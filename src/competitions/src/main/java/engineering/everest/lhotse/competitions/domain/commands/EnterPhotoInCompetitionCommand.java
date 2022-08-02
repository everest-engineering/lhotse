package engineering.everest.lhotse.competitions.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterPhotoInCompetitionCommand implements Serializable {
    @TargetAggregateIdentifier
    private UUID competitionId;
    private UUID photoId;
    private UUID requestingUserId;
    private UUID photoOwnerUserId;
    private String submissionNotes;
}
