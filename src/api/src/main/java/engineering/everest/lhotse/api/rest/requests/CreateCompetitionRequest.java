package engineering.everest.lhotse.api.rest.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompetitionRequest {
    @NotBlank
    private String description;
    @NotNull
    @Schema(required = true)
    private Instant submissionsOpenTimestamp;
    @NotNull
    @Schema(required = true)
    private Instant submissionsCloseTimestamp;
    @NotNull
    @Schema(required = true)
    private Instant votingEndsTimestamp;
    @Schema(required = true)
    private int maxEntriesPerUser;
}
