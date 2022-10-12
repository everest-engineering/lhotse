package engineering.everest.lhotse.api.rest.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionSubmissionRequest {
    @NotNull
    @Schema(required = true)
    private UUID photoId;
    private String submissionNotes;
}
