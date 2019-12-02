package engineering.everest.starterkit.organizations.persistence;

import engineering.everest.starterkit.organizations.OrganizationAddress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OrganizationsRepository extends MongoRepository<PersistableOrganization, UUID> {

    default void createOrganization(UUID id, String organizationName, OrganizationAddress organizationAddress,
                                    String websiteUrl, String contactName, String phoneNumber, String emailAddress,
                                    Instant registeredOn) {
        var address = new Address(organizationAddress.getStreet(), organizationAddress.getCity(),
                organizationAddress.getState(), organizationAddress.getCountry(), organizationAddress.getPostalCode());

        save(new PersistableOrganization(id, organizationName, address, websiteUrl, contactName, phoneNumber, emailAddress,
                registeredOn));
    }
}
