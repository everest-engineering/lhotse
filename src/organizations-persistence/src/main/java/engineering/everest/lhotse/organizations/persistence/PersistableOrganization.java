package engineering.everest.lhotse.organizations.persistence;

import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.OrganizationAddress;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity(name = "organizations")
public class PersistableOrganization {

    @Id
    private UUID id;
    private String organizationName;
    private Instant registeredOn;
    @Embedded
    private Address address;
    private String websiteUrl;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;
    private boolean disabled;

    public PersistableOrganization(UUID id,
                                   String organizationName,
                                   Address address,
                                   String websiteUrl,
                                   String contactName,
                                   String phoneNumber,
                                   String emailAddress,
                                   boolean isDisabled,
                                   Instant registeredOn) {
        this.id = id;
        this.organizationName = organizationName;
        this.registeredOn = registeredOn;
        this.address = address;
        this.websiteUrl = websiteUrl;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.disabled = isDisabled;
    }

    public Organization toDomain() {
        return new Organization(id, organizationName,
            new OrganizationAddress(address.getStreet(), address.getCity(), address.getState(), address.getCountry(),
                address.getPostalCode()),
            websiteUrl, contactName, phoneNumber, emailAddress, isDisabled());
    }
}
