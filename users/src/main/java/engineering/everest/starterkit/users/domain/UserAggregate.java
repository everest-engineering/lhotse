package engineering.everest.starterkit.users.domain;

import engineering.everest.starterkit.users.domain.commands.CreateUserCommand;
import engineering.everest.starterkit.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.starterkit.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.starterkit.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.users.domain.events.UserProfilePhotoUploadedEvent;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(repository = "repositoryForUser")
@Log4j2
public class UserAggregate implements Serializable {

    @AggregateIdentifier
    private UUID userId;
    private UUID userOnOrganizationId;
    private String userEmail;
    private String displayName;
    private boolean disabled;

    public UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        apply(new UserCreatedByAdminEvent(command.getUserId(), command.getOrganizationId(),
                command.getRequestingUserId(), command.getUserDisplayName(), command.getEmailAddress(),
                command.getEncodedPassword()));
    }

    @CommandHandler
    void handle(UpdateUserDetailsCommand command) {
        validateUserIsNotDisabled();
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
        validateUserIsNotDisabled();
        apply(new UserProfilePhotoUploadedEvent(command.getUserId(), command.getProfilePhotoFileId()));
        return command.getProfilePhotoFileId();
    }

    @EventSourcingHandler
    void on(UserCreatedByAdminEvent event) {
        userId = event.getUserId();
        userEmail = event.getUserEmail();
        displayName = event.getUserDisplayName();
        userOnOrganizationId = event.getOrganizationId();
        disabled = false;
    }

    @EventSourcingHandler
    void on(UserDetailsUpdatedByAdminEvent event) {
        userEmail = selectDesiredState(event.getEmailChange(), userEmail);
        displayName = selectDesiredState(event.getDisplayNameChange(), displayName);
    }

    private void validateUserIsNotDisabled() {
        Validate.validState(!disabled, "Account is disabled");
    }

    private void validateDisplayNameIsPresent(String displayName) {
        Validate.isTrue(!isBlank(displayName), "User display name is required");
    }

    private void validateAtLeastOneChangeIsBeingMade(UpdateUserDetailsCommand command) {
        boolean changesMade = command.getDisplayNameChange() != null
                || command.getEmailChange() != null
                || command.getPasswordChange() != null;
        Validate.isTrue(changesMade, "At least one user field change must be requested");
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null ? currentState : desiredState;
    }
}
