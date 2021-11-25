package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUploadedUserProfilePhotoCommand implements UsersStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private UUID profilePhotoFileId;

    @Override
    public Set<UUID> getUserIds() {
        return Set.of(userId);
    }
}
