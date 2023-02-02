package engineering.everest.lhotse.users.domain.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

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
