package engineering.everest.lhotse.users.services;

import java.util.UUID;

public interface UsersService {

    void deleteAndForgetUser(UUID requestingUserId, UUID userIdToDelete, String requestReason);
}
