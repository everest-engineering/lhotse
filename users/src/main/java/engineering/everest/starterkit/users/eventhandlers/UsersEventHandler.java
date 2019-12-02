package engineering.everest.starterkit.users.eventhandlers;

import engineering.everest.starterkit.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.starterkit.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.users.persistence.UsersRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UsersEventHandler {

    private final UsersRepository usersRepository;

    @Autowired
    public UsersEventHandler(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @EventHandler
    void on(UserCreatedByAdminEvent event, @Timestamp Instant creationTime) {
        usersRepository.createUser(event.getUserId(), event.getOrganizationId(), event.getUserDisplayName(),
                event.getUserEmail(), event.getEncodedPassword(), creationTime);
    }

    @EventHandler
    void on(UserDetailsUpdatedByAdminEvent event) {
        var persistableUser = usersRepository.findById(event.getUserId()).orElseThrow();

        persistableUser.setDisplayName(selectDesiredState(event.getDisplayNameChange(), persistableUser.getDisplayName()));
        persistableUser.setEmail(selectDesiredState(event.getEmailChange(), persistableUser.getEmail()));
        persistableUser.setEncodedPassword(selectDesiredState(event.getEncodedPasswordChange(), persistableUser.getEncodedPassword()));

        usersRepository.save(persistableUser);
    }

    @EventHandler
    void on(UserProfilePhotoUploadedEvent event) {
        var persistableUser = usersRepository.findById(event.getUserId()).orElseThrow();
        persistableUser.setProfilePhotoFileId(event.getProfilePhotoFileId());
        usersRepository.save(persistableUser);
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null ? currentState : desiredState;
    }
}
