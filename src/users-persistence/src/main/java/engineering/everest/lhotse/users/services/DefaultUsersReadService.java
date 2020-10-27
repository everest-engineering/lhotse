package engineering.everest.lhotse.users.services;

import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class DefaultUsersReadService implements UsersReadService {

    private final UsersRepository usersRepository;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;

    @Autowired
    public DefaultUsersReadService(UsersRepository usersRepository,
                                   FileService fileService,
                                   ThumbnailService thumbnailService) {
        this.usersRepository = usersRepository;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public User getById(UUID id) {
        return convert(usersRepository.findById(id).orElseThrow());
    }

    @Override
    public List<User> getUsers() {
        return usersRepository.findAll().stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public List<User> getUsersForOrganization(UUID organizationId) {
        return usersRepository.findByOrganizationId(organizationId).stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public boolean exists(UUID userId) {
        return usersRepository.existsById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return convert(usersRepository.findByEmailIgnoreCase(username).orElseThrow());
    }

    @Override
    public boolean hasUserWithEmail(String email) {
        return usersRepository.findByEmailIgnoreCase(email).isPresent();
    }

    @Override
    public InputStream getProfilePhotoThumbnailStream(UUID userId, int width, int height) throws IOException {
        PersistableUser persistableUser = usersRepository.findById(userId).orElseThrow();
        UUID profilePhotoFileId = persistableUser.getProfilePhotoFileId();
        if (profilePhotoFileId == null) {
            throw new NoSuchElementException("Profile photo not present");
        }
        return thumbnailService.streamThumbnailForOriginalFile(profilePhotoFileId, width, height);
    }

    @Override
    public InputStream getProfilePhotoStream(UUID id) throws IOException {
        PersistableUser persistableUser = usersRepository.findById(id).orElseThrow();
        UUID profilePhotoFileId = persistableUser.getProfilePhotoFileId();
        if (profilePhotoFileId == null) {
            throw new NoSuchElementException("Profile photo not present");
        }
        return fileService.stream(profilePhotoFileId).getInputStream();
    }

    private User convert(PersistableUser persistableUser) {
        return new User(persistableUser.getId(), persistableUser.getOrganizationId(), persistableUser.getUsername(),
                persistableUser.getDisplayName(), persistableUser.getEmail(),
                persistableUser.isDisabled(), persistableUser.getRoles());
    }
}
