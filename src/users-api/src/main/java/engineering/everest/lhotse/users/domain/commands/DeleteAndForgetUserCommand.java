package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAndForgetUserCommand implements ValidatableCommand {

    @TargetAggregateIdentifier
    private UUID userId;
    @NotNull
    private UUID requestingUserId;
    @NotBlank
    private String requestReason;
}
