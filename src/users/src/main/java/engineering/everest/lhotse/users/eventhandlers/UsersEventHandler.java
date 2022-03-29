package engineering.everest.lhotse.users.eventhandlers;

import engineering.everest.axon.cryptoshredding.CryptoShreddingKeyService;
import engineering.everest.axon.cryptoshredding.TypeDifferentiatedSecretKeyId;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;

@Service
@Slf4j
public class UsersEventHandler implements ReplayCompletionAware {

    private final UsersRepository usersRepository;
    private final CryptoShreddingKeyService cryptoShreddingKeyService;

    @Autowired
    public UsersEventHandler(UsersRepository usersRepository, CryptoShreddingKeyService cryptoShreddingKeyService) {
        this.usersRepository = usersRepository;
        this.cryptoShreddingKeyService = cryptoShreddingKeyService;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", UsersEventHandler.class.getSimpleName());
        usersRepository.deleteByIdNot(ADMIN_ID);
    }

    @EventHandler
    void on(UserCreatedByAdminEvent event, @Timestamp Instant creationTime) {
        LOGGER.info("User {} created by admin {} on organization {}", event.getUserId(), event.getAdminId(), event.getOrganizationId());
        var userEmail = event.getUserEmail() == null
            ? String.format("%s@deleted", event.getUserId())
            : event.getUserEmail();
        usersRepository.createUser(event.getUserId(), event.getOrganizationId(), event.getUserDisplayName(), userEmail, creationTime);
    }

    @EventHandler
    void on(UserCreatedForNewlyRegisteredOrganizationEvent event, @Timestamp Instant creationTime) {
        LOGGER.info("User {} created for self registered organization {}", event.getUserId(), event.getOrganizationId());
        var userEmail = event.getUserEmail() == null
            ? String.format("%s@deleted", event.getUserId())
            : event.getUserEmail();

        usersRepository.createUser(event.getUserId(), event.getOrganizationId(), event.getUserDisplayName(),
            userEmail, creationTime);
    }

    @EventHandler
    void on(UserDetailsUpdatedByAdminEvent event) {
        LOGGER.info("User {} details updated by admin {}", event.getUserId(), event.getAdminId());
        var persistableUser = usersRepository.findById(event.getUserId()).orElseThrow();
        persistableUser.setDisplayName(selectDesiredState(event.getDisplayNameChange(),
            persistableUser.getDisplayName()));
        persistableUser.setEmailAddress(selectDesiredState(event.getEmailChange(), persistableUser.getEmailAddress()));
        usersRepository.save(persistableUser);
    }

    @EventHandler
    void on(UserProfilePhotoUploadedEvent event) {
        LOGGER.info("User {} uploaded photo with fileId {}", event.getUserId(), event.getProfilePhotoFileId());
        var persistableUser = usersRepository.findById(event.getUserId()).orElseThrow();
        persistableUser.setProfilePhotoFileId(event.getProfilePhotoFileId());
        usersRepository.save(persistableUser);
    }

    @EventHandler
    void on(UserDeletedAndForgottenEvent event) {
        LOGGER.info("Deleting user {}", event.getDeletedUserId());
        usersRepository.deleteById(event.getDeletedUserId());
        cryptoShreddingKeyService.deleteSecretKey(
            new TypeDifferentiatedSecretKeyId(event.getDeletedUserId().toString(), ""));
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null
            ? currentState
            : desiredState;
    }
}
