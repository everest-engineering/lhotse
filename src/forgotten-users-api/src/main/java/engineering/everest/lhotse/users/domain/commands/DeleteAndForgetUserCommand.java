package engineering.everest.lhotse.users.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAndForgetUserCommand implements Serializable {
    @TargetAggregateIdentifier
    private UUID userIdToDelete;
    @NotNull
    private UUID requestingUserId;
    @NotBlank
    private String requestReason;
}
