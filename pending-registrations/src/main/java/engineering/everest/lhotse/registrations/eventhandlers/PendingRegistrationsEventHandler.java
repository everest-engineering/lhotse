package engineering.everest.lhotse.registrations.eventhandlers;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmationEmailSentEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
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

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", PendingRegistrationsEventHandler.class.getSimpleName());
    }

    @EventHandler
    void on(OrganizationRegistrationReceivedEvent event, @Timestamp Instant creationTime) {
        LOGGER.info("Creating pending registration for organization {}", event.getOrganizationId());
    }

    @EventHandler
    @DisallowReplay
    void on(OrganizationRegistrationConfirmationEmailSentEvent event) {
        LOGGER.info("Sent organization {} registration confirmation email with confirmation code {}",
                event.getOrganizationId(), event.getRegistrationConfirmationCode());
    }

    @EventHandler
    void on(OrganizationRegistrationConfirmedEvent event) {
        LOGGER.info("Organization {} registration confirmed, enabling it", event.getOrganizationId());
    }
}
