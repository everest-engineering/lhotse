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

    private UUID id;
    private UUID organizationId;
    private String displayName;
    private String emailAddress;
    private boolean disabled;

    public User(UUID id, UUID organizationId, String displayName, String emailAddress) {
        this(id, organizationId, displayName, emailAddress, false);
    }
}
