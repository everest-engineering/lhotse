package engineering.everest.starterkit.organizations.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "organizations")
public class PersistableOrganization {

    @Id
    private UUID id;
    private String organizationName;
    private Instant registeredOn;
    private Address address;
    private String websiteUrl;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;
    private boolean deregistered;

    public PersistableOrganization(UUID id, String organizationName, Address address, String websiteUrl,
                                   String contactName, String phoneNumber, String emailAddress, Instant registeredOn) {
        this.id = id;
        this.organizationName = organizationName;
        this.registeredOn = registeredOn;
        this.address = address;
        this.websiteUrl = websiteUrl;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
}
