package engineering.everest.starterkit.axon.users.eventhandlers;

import engineering.everest.starterkit.axon.users.domain.events.OrgAdminRemovedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserAccountStatusUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.starterkit.axon.users.domain.events.OrgAdminAssignedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.persistence.UsersRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static engineering.everest.starterkit.axon.common.domain.Role.ORG_ADMIN;

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
    void on(UserAccountStatusUpdatedByAdminEvent event) {
        var user = usersRepository.findById(event.getDisabledUserId()).orElseThrow();
        user.setDisabled(event.isDisabled());
        usersRepository.save(user);
    }

    @EventHandler
    void on(OrgAdminAssignedByAdminEvent event) {
        var user = usersRepository.findById(event.getAssignedUserId()).orElseThrow();
        user.addRole(ORG_ADMIN);
        usersRepository.save(user);
    }

    @EventHandler
    void on(OrgAdminRemovedByAdminEvent event) {
        var user = usersRepository.findById(event.getRemovedUserId()).orElseThrow();
        user.removeRole(ORG_ADMIN);
        usersRepository.save(user);
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
