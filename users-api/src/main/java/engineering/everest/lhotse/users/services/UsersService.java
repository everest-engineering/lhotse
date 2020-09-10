package engineering.everest.lhotse.users.services;

import java.util.UUID;

public interface UsersService {

    UUID createUser(UUID requestingUserId, UUID organizationId, String username, String displayName, String rawPassword);

    void updateUser(UUID requestingUserId, UUID userId, String emailChange,
                    String displayNameChange, String passwordChange);

    void storeProfilePhoto(UUID requestingUserId, UUID profilePhotoFileId);

    void deleteAndForget(UUID requestingUserId, UUID userId, String requestReason);
}
