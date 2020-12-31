package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameChangedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.TranslatingValidator.isTrue;
import static engineering.everest.lhotse.i18n.TranslatingValidator.isValidState;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(repository = "repositoryForOrganization")
public class OrganizationAggregate implements Serializable {

    @AggregateIdentifier
    private UUID id;
    private String organizationName;
    @AggregateMember
    private OrganizationContactDetails organizationContactDetails = new OrganizationContactDetails();
    private boolean disabled;
    private Set<UUID> organizationAdminIds;

    protected OrganizationAggregate() {
    }

    @CommandHandler
    public OrganizationAggregate(CreateRegisteredOrganizationCommand command) {
        apply(new OrganizationRegisteredEvent(command.getOrganizationId(), command.getRequestingUserId(),
                command.getOrganizationName(), command.getWebsiteUrl(), command.getStreet(), command.getCity(), command.getState(),
                command.getCountry(), command.getPostalCode(), command.getContactName(), command.getPhoneNumber(),
                command.getEmailAddress()));
    }

    @CommandHandler
    void handle(PromoteUserToOrganizationAdminCommand command) {
        validateOrganizationIsEnabled();
        isTrue(!organizationAdminIds.contains(command.getPromotedUserId()),
                "USER_ALREADY_ORGANIZATION_ADMIN", command.getPromotedUserId(), id);

        apply(new UserPromotedToOrganizationAdminEvent(command.getOrganizationId(), command.getPromotedUserId()));
    }

    @CommandHandler
    void handle(DisableOrganizationCommand command) {
        validateOrganizationIsEnabled();
        apply(new OrganizationDisabledByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    void handle(EnableOrganizationCommand command) {
        isValidState(disabled, "ORGANIZATION_ALREADY_ENABLED", id);
        apply(new OrganizationEnabledByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    public void handle(UpdateOrganizationCommand command) {
        validateOrganizationIsEnabled();
        validateAtLeastOneUpdateIsMade(command);

        if (isNameUpdated(command)) {
            apply(new OrganizationNameChangedEvent(command.getOrganizationId(), command.getOrganizationName(),
                    command.getRequestingUserId()));
        }
        if (areContactDetailsUpdated(command)) {
            apply(new OrganizationContactDetailsUpdatedEvent(command.getOrganizationId(), command.getContactName(),
                    command.getPhoneNumber(), command.getEmailAddress(), command.getWebsiteUrl(), command.getRequestingUserId()));
        }
        if (isAddressUpdated(command)) {
            apply(new OrganizationAddressUpdatedEvent(command.getOrganizationId(), command.getStreet(), command.getCity(),
                    command.getState(), command.getCountry(), command.getPostalCode(), command.getRequestingUserId()));
        }
    }

    @EventSourcingHandler
    void on(OrganizationRegisteredEvent event) {
        id = event.getOrganizationId();
        organizationName = event.getOrganizationName();
        organizationAdminIds = new HashSet<>();
        disabled = false;
    }

    @EventSourcingHandler
    void on(OrganizationDisabledByAdminEvent event) {
        disabled = true;
    }

    @EventSourcingHandler
    void on(OrganizationEnabledByAdminEvent event) {
        disabled = false;
    }

    @EventSourcingHandler
    void on(UserPromotedToOrganizationAdminEvent event) {
        organizationAdminIds.add(event.getPromotedUserId());
    }

    private void validateOrganizationIsEnabled() {
        isValidState(!disabled, "ORGANIZATION_IS_DISABLED", id);
    }

    private void validateAtLeastOneUpdateIsMade(UpdateOrganizationCommand command) {
        isTrue(isNameUpdated(command) || areContactDetailsUpdated(command) || isAddressUpdated(command),
                "ORGANIZATION_UPDATE_NO_FIELDS_CHANGED");
    }

    private boolean isNameUpdated(UpdateOrganizationCommand command) {
        return command.getOrganizationName() != null;
    }

    private boolean areContactDetailsUpdated(UpdateOrganizationCommand command) {
        return command.getWebsiteUrl() != null
                || command.getContactName() != null
                || command.getPhoneNumber() != null
                || command.getEmailAddress() != null;
    }

    private boolean isAddressUpdated(UpdateOrganizationCommand command) {
        return command.getStreet() != null
                || command.getCity() != null
                || command.getState() != null
                || command.getCountry() != null
                || command.getPostalCode() != null;
    }
}
