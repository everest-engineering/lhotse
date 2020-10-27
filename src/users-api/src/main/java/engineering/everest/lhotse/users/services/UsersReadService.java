package engineering.everest.lhotse.users.services;

import engineering.everest.lhotse.axon.common.services.ReadService;
import engineering.everest.lhotse.axon.common.domain.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface UsersReadService extends ReadService<User> {

    List<User> getUsers();

    List<User> getUsersForOrganization(UUID organizationId);

    boolean exists(UUID userId);

    User getUserByUsername(String username);

    boolean hasUserWithEmail(String email);

    InputStream getProfilePhotoStream(UUID id) throws IOException;

    InputStream getProfilePhotoThumbnailStream(UUID userId, int width, int height) throws IOException;
}
