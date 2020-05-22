package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationReceivedEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@ToString
class OrganizationContactDetails implements Serializable {

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
    void on(OrganizationRegisteredByAdminEvent event) {
        contactName = event.getContactName();
        contactEmail = event.getContactEmail();
        contactPhoneNumber = event.getContactPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationRegistrationReceivedEvent event) {
        contactName = event.getContactName();
        contactEmail = event.getRegisteringContactEmail();
        contactPhoneNumber = event.getContactPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationContactDetailsUpdatedByAdminEvent event) {
        contactName = event.getContactName();
        contactEmail = event.getEmailAddress();
        contactPhoneNumber = event.getPhoneNumber();
        websiteUrl = event.getWebsiteUrl();
    }

    @EventSourcingHandler
    void on(OrganizationAddressUpdatedByAdminEvent event) {
        city = event.getCity();
        country = event.getCountry();
        postalCode = event.getPostalCode();
        state = event.getState();
        street = event.getStreet();
    }
}
