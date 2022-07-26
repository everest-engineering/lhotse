package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private UUID id;
    private String filename;
    private Instant uploadTimestamp;
}
