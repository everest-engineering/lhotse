package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.users.domain.commands.AddUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.CreateOrganizationUserCommand;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.RemoveUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesAddedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesRemovedByAdminEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.USER_DISPLAY_NAME_MISSING;
import static engineering.everest.lhotse.i18n.MessageKeys.USER_UPDATE_NO_FIELDS_CHANGED;
import static engineering.everest.lhotse.i18n.MessageKeys.USER_UPDATE_NO_ROLES_SPECIFIED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(repository = "repositoryForUser")
public class UserAggregate implements Serializable {

    @AggregateIdentifier
    private UUID userId;
    private UUID userOnOrganizationId;
    private String userEmail;
    private String displayName;

    protected UserAggregate() {}

    @CommandHandler
    public UserAggregate(CreateUserForNewlyRegisteredOrganizationCommand command) {
        apply(new UserCreatedForNewlyRegisteredOrganizationEvent(command.getOrganizationId(), command.getUserId(),
            command.getDisplayName(), command.getUserEmail()));
    }

    @CommandHandler
    public UserAggregate(CreateOrganizationUserCommand command) {
        apply(new UserCreatedByAdminEvent(command.getUserId(), command.getOrganizationId(),
            command.getRequestingUserId(), command.getUserDisplayName(), command.getEmailAddress()));
    }

    @CommandHandler
    void handle(UpdateUserDetailsCommand command) {
        validateAtLeastOneChangeIsBeingMade(command);

        if (command.getDisplayNameChange() != null) {
            validateDisplayNameIsPresent(command.getDisplayNameChange());
        }
        apply(new UserDetailsUpdatedByAdminEvent(command.getUserId(), userOnOrganizationId,
            command.getDisplayNameChange(), command.getEmailChange(), command.getRequestingUserId()));
    }

    @CommandHandler
    void handle(AddUserRolesCommand command) {
        validateAtLeastOneRoleIsPresent(command);

        apply(new UserRolesAddedByAdminEvent(command.getUserId(), command.getRoles(), command.getRequestingUserId()));
    }

    @CommandHandler
    void handle(RemoveUserRolesCommand command) {
        validateAtLeastOneRoleIsPresent(command);

        apply(new UserRolesRemovedByAdminEvent(command.getUserId(), command.getRoles(), command.getRequestingUserId()));
    }

    @CommandHandler
    UUID handle(RegisterUploadedUserProfilePhotoCommand command) {
        apply(new UserProfilePhotoUploadedEvent(command.getUserId(), command.getProfilePhotoFileId()));
        return command.getProfilePhotoFileId();
    }

    @CommandHandler
    void handle(DeleteAndForgetUserCommand command) {
        apply(new UserDeletedAndForgottenEvent(command.getUserId(), command.getRequestingUserId(),
            command.getRequestReason()));
    }

    @EventSourcingHandler
    void on(UserRolesAddedByAdminEvent event) {
        userId = event.getRequestingUserId();
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
            throw new TranslatableIllegalArgumentException(USER_DISPLAY_NAME_MISSING);
        }
    }

    private void validateAtLeastOneChangeIsBeingMade(UpdateUserDetailsCommand command) {
        boolean changesMade = command.getDisplayNameChange() != null
            || command.getEmailChange() != null;
        if (!changesMade) {
            throw new TranslatableIllegalArgumentException(USER_UPDATE_NO_FIELDS_CHANGED);
        }
    }

    private void validateAtLeastOneRoleIsPresent(AddUserRolesCommand command) {
        if (command.getRoles().isEmpty()) {
            throw new TranslatableIllegalArgumentException(USER_UPDATE_NO_ROLES_SPECIFIED);
        }
    }

    private void validateAtLeastOneRoleIsPresent(RemoveUserRolesCommand command) {
        if (command.getRoles().isEmpty()) {
            throw new TranslatableIllegalArgumentException(USER_UPDATE_NO_ROLES_SPECIFIED);
        }
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null
            ? currentState
            : desiredState;
    }
}
