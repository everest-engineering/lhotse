package engineering.everest.lhotse.organizations.domain.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationQuery {
    private UUID organizationId;
}
