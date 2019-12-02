package engineering.everest.starterkit.axon.users.domain;

import engineering.everest.starterkit.axon.users.domain.events.OrgAdminRemovedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserAccountStatusUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.axon.users.domain.commands.CreateUserCommand;
import engineering.everest.starterkit.axon.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.starterkit.axon.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.starterkit.axon.users.domain.events.OrgAdminAssignedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserAssignedAsExpertToOrganizationEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserRemovedAsExpertToOrganizationEvent;
import engineering.everest.starterkit.axon.users.services.UsersReadService;
import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(repository = "repositoryForUser")
public class UserAggregate implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAggregate.class);

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

    // TODO: should check organization status for all user editing commands?
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
        LOGGER.info("User id: {} on organization id:{} uploaded photo with fileId: {}", command.getUserId(), userOnOrganizationId,
                command.getProfilePhotoFileId());

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

    @EventSourcingHandler
    void on(UserAccountStatusUpdatedByAdminEvent event) {
        disabled = event.isDisabled();
    }

    @EventSourcingHandler
    void on(UserAssignedAsExpertToOrganizationEvent event) {
    }

    @EventSourcingHandler
    void on(UserRemovedAsExpertToOrganizationEvent event) {
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
