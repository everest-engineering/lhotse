package engineering.everest.lhotse.api.rest.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompetitionRequest {
    private String description;
    @NotBlank
    @Schema(required = true)
    private Instant submissionsOpenTimestamp;
    @NotBlank
    @Schema(required = true)
    private Instant submissionsCloseTimestamp;
    @NotBlank
    @Schema(required = true)
    private Instant votingEndsTimestamp;
    @NotBlank
    @Schema(required = true)
    private int maxEntriesPerUser;
}
