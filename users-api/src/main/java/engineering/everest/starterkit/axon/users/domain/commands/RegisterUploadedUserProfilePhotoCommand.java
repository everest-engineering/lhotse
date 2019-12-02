package engineering.everest.starterkit.axon.users.domain.commands;

import engineering.everest.starterkit.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUploadedUserProfilePhotoCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private UUID profilePhotoFileId;
}
