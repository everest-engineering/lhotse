package engineering.everest.lhotse.photos.domain.commands;

import engineering.everest.lhotse.axon.command.validation.FileStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUploadedPhotoCommand implements FileStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID photoId;
    private UUID owningUserId;
    private UUID backingFileId;
    private String filename;

    @Override
    public Set<UUID> getFileIDs() {
        return Set.of(backingFileId);
    }
}
