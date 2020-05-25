package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrganizationRegistrationResponse {
    private UUID newOrganizationId;
    private UUID newUserId;
}
