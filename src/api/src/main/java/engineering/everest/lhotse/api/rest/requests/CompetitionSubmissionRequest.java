package engineering.everest.lhotse.api.rest.requests;

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
    private UUID photoId;
    private String submissionNotes;
}
