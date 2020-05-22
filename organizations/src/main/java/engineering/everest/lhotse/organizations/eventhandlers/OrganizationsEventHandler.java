package engineering.everest.lhotse.organizations.eventhandlers;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import engineering.everest.lhotse.organizations.persistence.Address;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import lombok.extern.log4j.Log4j2;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrganizationsEventHandler implements ReplayCompletionAware {

    private final OrganizationsRepository organizationsRepository;

    @Autowired
    public OrganizationsEventHandler(OrganizationsRepository organizationsRepository) {
        this.organizationsRepository = organizationsRepository;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", OrganizationsEventHandler.class.getSimpleName());
        organizationsRepository.deleteAll();
    }

    @EventHandler
    void on(OrganizationRegisteredByAdminEvent event, @Timestamp Instant creationTime) {
        LOGGER.info("Creating new organization {}", event.getOrganizationId());
        var organizationAddress = new OrganizationAddress(event.getStreet(), event.getCity(), event.getState(),
                event.getCountry(), event.getPostalCode());
        organizationsRepository.createOrganization(event.getOrganizationId(), event.getOrganizationName(),
                organizationAddress, event.getWebsiteUrl(), event.getContactName(), event.getContactPhoneNumber(),
                event.getContactEmail(), creationTime);
    }

    @EventHandler
    void on(OrganizationDisabledByAdminEvent event) {
        LOGGER.info("Organization {} de-registered by {}", event.getOrganizationId(), event.getAdminId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization.setDeregistered(true);
        organizationsRepository.save(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationEnabledByAdminEvent event) {
        LOGGER.info("Organization {} re-registered by {}", event.getOrganizationId(), event.getAdminId());
        var persistableOrganization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        persistableOrganization.setDeregistered(false);
        organizationsRepository.save(persistableOrganization);
    }

    @EventHandler
    void on(OrganizationNameUpdatedByAdminEvent event) {
        LOGGER.info("Organization {} name updated by {}", event.getOrganizationId(), event.getAdminId());
        var organization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        organization.setOrganizationName(selectDesiredState(event.getOrganizationName(), organization.getOrganizationName()));
        organizationsRepository.save(organization);
    }

    @EventHandler
    void on(OrganizationContactDetailsUpdatedByAdminEvent event) {
        LOGGER.info("Organization {} contact details updated by {}", event.getOrganizationId(), event.getAdminId());
        var organization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        organization.setContactName(selectDesiredState(event.getContactName(), organization.getContactName()));
        organization.setPhoneNumber(selectDesiredState(event.getPhoneNumber(), organization.getPhoneNumber()));
        organization.setEmailAddress(selectDesiredState(event.getEmailAddress(), organization.getEmailAddress()));
        organization.setWebsiteUrl(selectDesiredState(event.getWebsiteUrl(), organization.getWebsiteUrl()));
        organizationsRepository.save(organization);
    }

    @EventHandler
    void on(OrganizationAddressUpdatedByAdminEvent event) {
        LOGGER.info("Organization {} address updated by {}", event.getOrganizationId(), event.getAdminId());
        var organization = organizationsRepository.findById(event.getOrganizationId()).orElseThrow();
        var organizationAddress = organization.getAddress();
        String city = selectDesiredState(event.getCity(), organizationAddress.getCity());
        String street = selectDesiredState(event.getStreet(), organizationAddress.getStreet());
        String state = selectDesiredState(event.getState(), organizationAddress.getState());
        String country = selectDesiredState(event.getCountry(), organizationAddress.getCountry());
        String postalCode = selectDesiredState(event.getPostalCode(), organizationAddress.getPostalCode());
        Address address = new Address(street, city, state, country, postalCode);
        organization.setAddress(address);
        organizationsRepository.save(organization);
    }

    private String selectDesiredState(String desiredState, String currentState) {
        return desiredState == null ? currentState : desiredState;
    }
}
