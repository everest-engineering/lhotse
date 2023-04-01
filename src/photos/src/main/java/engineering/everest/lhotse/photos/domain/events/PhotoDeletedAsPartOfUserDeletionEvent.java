package engineering.everest.lhotse.photos.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class PhotoDeletedAsPartOfUserDeletionEvent {
    private UUID photoId;
    private UUID persistedFileId;
    private UUID deletedUserId;
}
