package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.registrations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmationEmailSentEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import org.apache.commons.lang3.Validate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(repository = "repositoryForPendingRegistration")
public class PendingRegistrationAggregate implements Serializable {

    @AggregateIdentifier
    private UUID registrationConfirmationCode;
    private UUID organizationId;

    protected PendingRegistrationAggregate() {
    }

    @CommandHandler
    public PendingRegistrationAggregate(RegisterOrganizationCommand command) {
        apply(new OrganizationRegistrationReceivedEvent(command.getOrganizationId(), command.getRegisteringUserId(),
                command.getRegistrationConfirmationCode(), command.getUserEmailAddress(), command.getUserEncodedPassword(),
                command.getOrganizationName(), command.getWebsiteUrl(), command.getStreet(), command.getCity(), command.getState(),
                command.getCountry(), command.getPostalCode(), command.getContactName(), command.getPhoneNumber()));
    }

    @CommandHandler
    void handle(RecordSentOrganizationRegistrationEmailConfirmationCommand command) {
        apply(new OrganizationRegistrationConfirmationEmailSentEvent(registrationConfirmationCode, organizationId,
                command.getRegisteringContactEmail(), command.getOrganizationName()));
    }

    @CommandHandler
    void handle(ConfirmOrganizationRegistrationEmailCommand command) {
        Validate.isTrue(organizationId.equals(command.getOrganizationId()), "Valid confirmation token for another organization");

        apply(new OrganizationRegistrationConfirmedEvent(registrationConfirmationCode, organizationId));
    }

    @EventSourcingHandler
    void on(OrganizationRegistrationReceivedEvent event) {
        registrationConfirmationCode = event.getRegistrationConfirmationCode();
        organizationId = event.getOrganizationId();
    }

    @EventSourcingHandler
    void on(OrganizationRegistrationConfirmedEvent event) {
        markDeleted();
    }
}
