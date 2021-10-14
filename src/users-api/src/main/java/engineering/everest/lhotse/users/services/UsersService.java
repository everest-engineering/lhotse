package engineering.everest.lhotse.users.services;
import engineering.everest.lhotse.axon.common.domain.Role;

import java.util.UUID;
import java.util.Set;


public interface UsersService {

    UUID createUser(UUID requestingUserId, UUID organizationId, String username, String displayName, String rawPassword);

    void updateUser(UUID requestingUserId, UUID userId, String emailChange,
                    String displayNameChange, String passwordChange);

    void updateUserRoles(UUID requestingUserId, UUID userId, Set<Role> roles);

    void storeProfilePhoto(UUID requestingUserId, UUID profilePhotoFileId);

    void deleteAndForget(UUID requestingUserId, UUID userId, String requestReason);
}
