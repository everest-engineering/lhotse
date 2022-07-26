package engineering.everest.lhotse.photos.domain.events;

import engineering.everest.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class PhotoUploadedEvent {
    private UUID photoId;
    @EncryptionKeyIdentifier
    private UUID owningUserId;
    private UUID backingFileId;
    @EncryptedField
    private String filename;
}
