package engineering.everest.lhotse.photos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    private UUID id;
    private UUID owningUserId;
    private UUID backingFileId;
    private String filename;
    private Instant uploadTimestamp;
}
