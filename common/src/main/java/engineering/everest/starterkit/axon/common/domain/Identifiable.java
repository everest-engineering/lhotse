package engineering.everest.starterkit.axon.common.domain;

import java.util.UUID;

public interface Identifiable {
    UUID getId();

    default boolean canRead(User user) {
        return false;
    }

    default boolean canCreate(User user) {
        return false;
    }

    default boolean canUpdate(User user) {
        return false;
    }

    default boolean canDelete(User user) {
        return false;
    }

}
