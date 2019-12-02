package engineering.everest.starterkit.organizations.domain;

import engineering.everest.starterkit.organizations.domain.commands.DeregisterOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.commands.ReregisterOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationDeregisteredByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationNameUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationRegisteredByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationReregisteredByAdminEvent;
import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(repository = "repositoryForOrganization")
public class OrganizationAggregate implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationAggregate.class);

    @AggregateIdentifier
    private UUID id;
    private String organizationName;
    private String organizationWebsiteUrl;
    @AggregateMember
    private OrganizationContactDetails organizationContactDetails = new OrganizationContactDetails();
    private boolean deregistered;

    protected OrganizationAggregate() {
    }

    @CommandHandler
    public OrganizationAggregate(RegisterOrganizationCommand command) {
        apply(new OrganizationRegisteredByAdminEvent(command.getOrganizationId(), command.getRequestingUserId(),
                command.getOrganizationName(), command.getWebsiteUrl(), command.getStreet(), command.getCity(), command.getState(),
                command.getCountry(), command.getPostalCode(), command.getContactName(), command.getPhoneNumber(),
                command.getEmailAddress()));
    }

    @CommandHandler
    void handle(DeregisterOrganizationCommand command) {
        validateOrganizationIsNotDeregistered();
        LOGGER.info("Organization id: {} de-registered by {}", command.getOrganizationId(), command.getRequestingUserId());
        apply(new OrganizationDeregisteredByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    void handle(ReregisterOrganizationCommand command) {
        Validate.validState(deregistered, "Organization is still active");
        LOGGER.info("Organization id: {} re-registered by {}", command.getOrganizationId(), command.getRequestingUserId());
        apply(new OrganizationReregisteredByAdminEvent(command.getOrganizationId(), command.getRequestingUserId()));
    }

    @CommandHandler
    public void handle(UpdateOrganizationCommand command) {
        validateOrganizationIsNotDeregistered();
        validateAtLeastOneUpdateIsMade(command);

        apply(new OrganizationNameUpdatedByAdminEvent(command.getOrganizationId(), command.getOrganizationName(),
                command.getRequestingUserId()));
        apply(new OrganizationContactDetailsUpdatedByAdminEvent(command.getOrganizationId(), command.getContactName(),
                command.getPhoneNumber(), command.getEmailAddress(), command.getWebsiteUrl(), command.getRequestingUserId()));
        apply(new OrganizationAddressUpdatedByAdminEvent(command.getOrganizationId(), command.getStreet(), command.getCity(),
                command.getState(), command.getCountry(), command.getPostalCode(), command.getRequestingUserId()));
    }

    @EventSourcingHandler
    void on(OrganizationRegisteredByAdminEvent event) {
        id = event.getOrganizationId();
        organizationName = event.getOrganizationName();
        organizationWebsiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationDeregisteredByAdminEvent event) {
        deregistered = true;
    }

    @EventSourcingHandler
    void on(OrganizationReregisteredByAdminEvent event) {
        deregistered = false;
    }

    @EventSourcingHandler
    void on(OrganizationNameUpdatedByAdminEvent event) {
        organizationName = event.getOrganizationName();
    }

    private void validateOrganizationIsNotDeregistered() {
        Validate.validState(!deregistered, "Organization is already deregistered");
    }

    private void validateAtLeastOneUpdateIsMade(UpdateOrganizationCommand command) {
        boolean changesMade = command.getOrganizationName() != null
                || command.getStreet() != null
                || command.getCity() != null
                || command.getState() != null
                || command.getCountry() != null
                || command.getPostalCode() != null
                || command.getWebsiteUrl() != null
                || command.getContactName() != null
                || command.getPhoneNumber() != null
                || command.getEmailAddress() != null;

        Validate.isTrue(changesMade, "At least one organization field change must be requested");
    }
}
