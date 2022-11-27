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
    @Schema
    private Instant submissionsOpenTimestamp;
    @NotNull
    @Schema
    private Instant submissionsCloseTimestamp;
    @NotNull
    @Schema
    private Instant votingEndsTimestamp;
    @Schema
    private int maxEntriesPerUser;
}
