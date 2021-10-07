package engineering.everest.lhotse.axon.common.domain;

import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAttribute {
    private UUID organizationId;
    private Set<Role> roles;
}
