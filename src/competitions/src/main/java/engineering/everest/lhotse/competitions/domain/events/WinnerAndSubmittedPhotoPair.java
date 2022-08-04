package engineering.everest.lhotse.competitions.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinnerAndSubmittedPhotoPair {
    private UUID winnerUserId;
    private UUID photoId;
}
