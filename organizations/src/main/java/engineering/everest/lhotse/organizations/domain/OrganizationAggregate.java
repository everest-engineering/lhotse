package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(repository = "repositoryForOrganization")
public class OrganizationAggregate implements Serializable {

    @AggregateIdentifier
    private UUID id;
    private String organizationName;
    @AggregateMember
    private OrganizationContactDetails organizationContactDetails = new OrganizationContactDetails();
    private boolean disabled;

    protected OrganizationAggregate() {
    }

    @CommandHandler
    public OrganizationAggregate(CreateRegisteredOrganizationCommand command) {
        apply(new OrganizationRegisteredByAdminEvent(command.getOrganizationId(), command.getRequestingUserId(),
                command.getOrganizationName(), command.getWebsiteUrl(), command.getStreet(), command.getCity(), command.getState(),
                command.getCountry(), command.getPostalCode(), command.getContactName(), command.getPhoneNumber(),
                command.getEmailAddress()));
    }

    @CommandHandler
    public OrganizationAggregate(RegisterOrganizationCommand command) {
        apply(new OrganizationRegistrationReceivedEvent(command.getOrganizationId(), command.getEmailAddress(),
                command.getOrganizationName(), command.getWebsiteUrl(), command.getStreet(), command.getCity(), command.getState(),
                command.getCountry(), command.getPostalCode(), command.getContactName(), command.getPhoneNumber()));
    }

    @CommandHandler
    void handle(DisableOrganizationCommand command) {
        validateOrganizationIsNotDisabled();
        apply(new OrganizationDisabledByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    void handle(EnableOrganizationCommand command) {
        Validate.validState(disabled, "Organization is already enabled");
        apply(new OrganizationEnabledByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    public void handle(UpdateOrganizationCommand command) {
        validateOrganizationIsNotDisabled();
        validateAtLeastOneUpdateIsMade(command);

        if (isNameUpdated(command)) {
            apply(new OrganizationNameUpdatedByAdminEvent(command.getOrganizationId(), command.getOrganizationName(),
                    command.getRequestingUserId()));
        }
        if (areContactDetailsUpdated(command)) {
            apply(new OrganizationContactDetailsUpdatedByAdminEvent(command.getOrganizationId(), command.getContactName(),
                    command.getPhoneNumber(), command.getEmailAddress(), command.getWebsiteUrl(), command.getRequestingUserId()));
        }
        if (isAddressUpdated(command)) {
            apply(new OrganizationAddressUpdatedByAdminEvent(command.getOrganizationId(), command.getStreet(), command.getCity(),
                    command.getState(), command.getCountry(), command.getPostalCode(), command.getRequestingUserId()));
        }
    }

    @EventSourcingHandler
    void on(OrganizationRegisteredByAdminEvent event) {
        id = event.getOrganizationId();
        organizationName = event.getOrganizationName();
        disabled = false;
    }

    @EventSourcingHandler
    void on(OrganizationRegistrationReceivedEvent event) {
        id = event.getOrganizationId();
        organizationName = event.getOrganizationName();
        disabled = true;
    }

    @EventSourcingHandler
    void on(OrganizationDisabledByAdminEvent event) {
        disabled = true;
    }

    @EventSourcingHandler
    void on(OrganizationEnabledByAdminEvent event) {
        disabled = false;
    }

    private void validateOrganizationIsNotDisabled() {
        Validate.validState(!disabled, "Organization is already disabled");
    }

    private void validateAtLeastOneUpdateIsMade(UpdateOrganizationCommand command) {
        Validate.isTrue(isNameUpdated(command) || areContactDetailsUpdated(command) || isAddressUpdated(command),
                "At least one organization field change must be requested");
    }

    private boolean isNameUpdated(UpdateOrganizationCommand command) {
        return command.getOrganizationName() != null && !command.getOrganizationName().equals(organizationName);
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
