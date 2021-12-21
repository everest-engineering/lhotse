package engineering.everest.lhotse.common.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAttribute {
    private UUID organizationId;
    private String displayName;
}
