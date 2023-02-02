package engineering.everest.lhotse.photos.persistence;

import engineering.everest.lhotse.photos.Photo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "photos")
public class PersistablePhoto {

    @Id
    private UUID id;
    private UUID ownerUserId;
    private UUID backingFileId;
    private String filename;
    private Instant uploadTimestamp;

    public Photo toDomain() {
        return new Photo(id, ownerUserId, backingFileId, filename, uploadTimestamp);
    }
}
