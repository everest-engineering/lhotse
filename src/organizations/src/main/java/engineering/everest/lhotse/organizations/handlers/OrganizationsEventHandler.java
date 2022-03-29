package engineering.everest.lhotse.organizations.handlers;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationCreatedForNewSelfRegisteredUserEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameChangedEvent;
import engineering.everest.lhotse.organizations.domain.queries.OrganizationQuery;
import engineering.everest.lhotse.organizations.persistence.Address;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class OrganizationsEventHandler implements ReplayCompletionAware {

    private final QueryUpdateEmitter queryUpdateEmitter;
    private final OrganizationsRepository organizationsRepository;

    @Autowired
    public OrganizationsEventHandler(QueryUpdateEmitter queryUpdateEmitter, OrganizationsRepository organizationsRepository) {
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.organizationsRepository = organizationsRepository;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", OrganizationsEventHandler.class.getSimpleName());
        organizationsRepository.deleteAll();
    }

    @EventHandler
    void on(OrganizationCreatedForNewSelfRegisteredUserEvent event, @Timestamp Instant creationTime) {
        LOGGER.info("Creating new registered organization {}", event.getOrganizationId());
        var organizationAddress = new OrganizationAddress(event.getStreet(), event.getCity(), event.getState(),
            event.getCountry(), event.getPostalCode());
        organizationsRepository.createOrganization(event.getOrganizationId(), event.getOrganizationName(),
            organizationAddress, event.getWebsiteUrl(), event.getContactName(), event.getContactPhoneNumber(),
            event.getContactEmail(), false, creationTime);
    }

    @EventHandler
    void on(OrganizationDisabledByAdminEvent event) {
        LOGGER.info("Organization {} disabled by {}", event.getOrganizationId(), event.getAdminId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization.setDisabled(true);
        organizationsRepository.save(persistableOrganization);

        emitOrganizationQueryUpdate(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationEnabledByAdminEvent event) {
        LOGGER.info("Organization {} enabled by {}", event.getOrganizationId(), event.getAdminId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization.setDisabled(false);
        organizationsRepository.save(persistableOrganization);

        emitOrganizationQueryUpdate(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationNameChangedEvent event) {
        LOGGER.info("Organization {} name updated by {}", event.getOrganizationId(), event.getUpdatingUserId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization
            .setOrganizationName(selectDesiredState(event.getOrganizationName(), persistableOrganization.getOrganizationName()));
        organizationsRepository.save(persistableOrganization);

        emitOrganizationQueryUpdate(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationContactDetailsUpdatedEvent event) {
        LOGGER.info("Organization {} contact details updated by {}", event.getOrganizationId(), event.getUpdatingUserId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization.setContactName(selectDesiredState(event.getContactName(), persistableOrganization.getContactName()));
        persistableOrganization.setPhoneNumber(selectDesiredState(event.getPhoneNumber(), persistableOrganization.getPhoneNumber()));
        persistableOrganization.setEmailAddress(selectDesiredState(event.getEmailAddress(), persistableOrganization.getEmailAddress()));
        persistableOrganization.setWebsiteUrl(selectDesiredState(event.getWebsiteUrl(), persistableOrganization.getWebsiteUrl()));
        organizationsRepository.save(persistableOrganization);

        emitOrganizationQueryUpdate(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationAddressUpdatedEvent event) {
        LOGGER.info("Organization {} address updated by {}", event.getOrganizationId(), event.getUpdatingUserId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        var organizationAddress = persistableOrganization.getAddress();
        var city = selectDesiredState(event.getCity(), organizationAddress.getCity());
        var street = selectDesiredState(event.getStreet(), organizationAddress.getStreet());
        var state = selectDesiredState(event.getState(), organizationAddress.getState());
        var country = selectDesiredState(event.getCountry(), organizationAddress.getCountry());
        var postalCode = selectDesiredState(event.getPostalCode(), organizationAddress.getPostalCode());
        var address = new Address(street, city, state, country, postalCode);
        persistableOrganization.setAddress(address);
        organizationsRepository.save(persistableOrganization);

        emitOrganizationQueryUpdate(persistableOrganization);
    }

    private void emitOrganizationQueryUpdate(PersistableOrganization persistableOrganization) {
        queryUpdateEmitter.emit(OrganizationQuery.class, query -> persistableOrganization.getId().equals(query.getOrganizationId()),
            persistableOrganization.toDomain());
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null
            ? currentState
            : desiredState;
    }
}
