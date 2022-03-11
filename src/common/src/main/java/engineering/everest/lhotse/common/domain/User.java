package engineering.everest.lhotse.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static java.util.UUID.fromString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
public class User implements Identifiable {

    public static final UUID ADMIN_ID = fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID MONITORING_ID = fromString("11111111-1111-1111-1111-111111111111");

    private UUID id;
    private UUID organizationId;
    private String username;
    private String displayName;
    private String email;
    private boolean disabled;

    public User(UUID id, UUID organizationId, String username, String displayName) {
        this(id, organizationId, username, displayName, false);
    }

    public User(UUID id, UUID organizationId, String username, String displayName, boolean disabled) {
        this(id, organizationId, username, displayName, username, disabled);
    }
}
