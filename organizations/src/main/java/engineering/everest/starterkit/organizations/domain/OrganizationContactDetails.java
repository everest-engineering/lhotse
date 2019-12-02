package engineering.everest.starterkit.organizations.domain;

import engineering.everest.starterkit.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@ToString
class OrganizationContactDetails implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationContactDetails.class);

    @EntityId
    private UUID organizationId;
    private String websiteUrl;
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    public OrganizationContactDetails() {
    }

    @EventSourcingHandler
    void on(OrganizationContactDetailsUpdatedByAdminEvent event) {
        LOGGER.info("Organization '{}' contact details updated by {}", event.getOrganizationId(), event.getAdminId());
        contactName = event.getContactName();
        contactEmail = event.getEmailAddress();
        contactPhoneNumber = event.getPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationAddressUpdatedByAdminEvent event) {
        LOGGER.info("Organization '{}' address updated by {}", event.getOrganizationId(), event.getAdminId());
        city = event.getCity();
        country = event.getCountry();
        postalCode = event.getPostalCode();
        state = event.getState();
        street = event.getStreet();
    }
}
