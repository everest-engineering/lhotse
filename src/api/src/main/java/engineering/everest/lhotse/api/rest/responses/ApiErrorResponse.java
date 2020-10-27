package engineering.everest.lhotse.api.rest.responses;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@Builder
public class ApiErrorResponse {

    private final HttpStatus status;
    private final String message;
    private final Instant timeStamp;
}
