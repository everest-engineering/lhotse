package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.i18n.TranslatableExceptionFactory;
import engineering.everest.lhotse.users.domain.commands.CreateUserCommand;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserProfilePhotoUploadedEvent;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.USER_DISPLAY_NAME_MISSING;
import static engineering.everest.lhotse.i18n.MessageKeys.USER_UPDATE_NO_FIELDS_CHANGED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(repository = "repositoryForUser")
@Log4j2
public class UserAggregate implements Serializable {

    @AggregateIdentifier
    private UUID userId;
    private UUID userOnOrganizationId;
    private String userEmail;
    private String displayName;

    protected UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        apply(new UserCreatedByAdminEvent(command.getUserId(), command.getOrganizationId(),
                command.getRequestingUserId(), command.getUserDisplayName(), command.getEmailAddress(),
                command.getEncodedPassword()));
    }

    @CommandHandler
    public UserAggregate(CreateUserForNewlyRegisteredOrganizationCommand command) {
        apply(new UserCreatedForNewlyRegisteredOrganizationEvent(command.getUserId(), command.getOrganizationId(),
                command.getRegistrationConfirmationCode(), command.getDisplayName(), command.getUserEmail(), command.getEncodedPassword()));
    }

    @CommandHandler
    void handle(UpdateUserDetailsCommand command) {
        validateAtLeastOneChangeIsBeingMade(command);

        if (command.getDisplayNameChange() != null) {
            validateDisplayNameIsPresent(command.getDisplayNameChange());
        }
        apply(new UserDetailsUpdatedByAdminEvent(command.getUserId(), userOnOrganizationId,
                command.getDisplayNameChange(), command.getEmailChange(), command.getPasswordChange(),
                command.getRequestingUserId()));
    }

    @CommandHandler
    UUID handle(RegisterUploadedUserProfilePhotoCommand command) {
        apply(new UserProfilePhotoUploadedEvent(command.getUserId(), command.getProfilePhotoFileId()));
        return command.getProfilePhotoFileId();
    }

    @CommandHandler
    void handle(DeleteAndForgetUserCommand command) {
        apply(new UserDeletedAndForgottenEvent(command.getUserId(), command.getRequestingUserId(), command.getRequestReason()));
    }

    @EventSourcingHandler
    void on(UserCreatedByAdminEvent event) {
        userId = event.getUserId();
        userEmail = event.getUserEmail();
        displayName = event.getUserDisplayName();
        userOnOrganizationId = event.getOrganizationId();
    }

    @EventSourcingHandler
    void on(UserCreatedForNewlyRegisteredOrganizationEvent event) {
        userId = event.getUserId();
        userEmail = event.getUserEmail();
        displayName = event.getUserDisplayName();
        userOnOrganizationId = event.getOrganizationId();
    }

    @EventSourcingHandler
    void on(UserDetailsUpdatedByAdminEvent event) {
        userEmail = selectDesiredState(event.getEmailChange(), userEmail);
        displayName = selectDesiredState(event.getDisplayNameChange(), displayName);
    }

    @EventSourcingHandler
    void on(UserDeletedAndForgottenEvent event) {
        userEmail = "";
        displayName = "";
        markDeleted();
    }

    private void validateDisplayNameIsPresent(String displayName) {
        if (isBlank(displayName)) {
            TranslatableExceptionFactory.throwForKey(USER_DISPLAY_NAME_MISSING);
        }
    }

    private void validateAtLeastOneChangeIsBeingMade(UpdateUserDetailsCommand command) {
        boolean changesMade = command.getDisplayNameChange() != null
                || command.getEmailChange() != null
                || command.getPasswordChange() != null;
        if (!changesMade) {
            TranslatableExceptionFactory.throwForKey(USER_UPDATE_NO_FIELDS_CHANGED);
        }
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null ? currentState : desiredState;
    }
}
