package engineering.everest.lhotse.registrations.eventhandlers;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCompletedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmationEmailSentEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.registrations.persistence.PendingRegistrationsRepository;
import lombok.extern.log4j.Log4j2;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PendingRegistrationsEventHandler implements ReplayCompletionAware {

    private final PendingRegistrationsRepository pendingRegistrationsRepository;

    public PendingRegistrationsEventHandler(PendingRegistrationsRepository pendingRegistrationsRepository) {
        this.pendingRegistrationsRepository = pendingRegistrationsRepository;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", PendingRegistrationsEventHandler.class.getSimpleName());
        pendingRegistrationsRepository.deleteAll();
    }

    @EventHandler
    void on(OrganizationRegistrationReceivedEvent event, @Timestamp Instant registrationReceivedTime) {
        LOGGER.info("Creating pending registration for organization {}", event.getOrganizationId());
        pendingRegistrationsRepository.createPendingRegistration(event.getRegistrationConfirmationCode(),
                event.getOrganizationId(), event.getRegisteringUserId(), event.getRegisteringContactEmail(),
                registrationReceivedTime);
    }

    @EventHandler
    @DisallowReplay
    void on(OrganizationRegistrationConfirmationEmailSentEvent event) {
        LOGGER.info("Sent organization {} registration confirmation email with confirmation code {}",
                event.getOrganizationId(), event.getRegistrationConfirmationCode());
    }

    @EventHandler
    void on(OrganizationRegistrationCompletedEvent event) {
        LOGGER.info("Organization {} registration completed, removing pending registration", event.getOrganizationId());
        pendingRegistrationsRepository.deleteById(event.getRegistrationConfirmationCode());
    }

    @EventHandler
    void on(OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent event) {
        LOGGER.info("Organization {} registration confirmed but user email already in use. Removing pending registration",
                event.getOrganizationId());
        pendingRegistrationsRepository.deleteById(event.getRegistrationConfirmationCode());
    }
}
