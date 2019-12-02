package engineering.everest.starterkit.organizations.persistence;

import java.util.UUID;

public class OrgAdminUser {

    private UUID userId;
    private String displayName;

    public OrgAdminUser() {
    }

    public OrgAdminUser(UUID userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
