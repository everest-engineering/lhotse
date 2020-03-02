package engineering.everest.lhotse.organizations.persistence;

import engineering.everest.lhotse.organizations.OrganizationAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OrganizationsRepository extends JpaRepository<PersistableOrganization, UUID> {

    default void createOrganization(UUID id, String organizationName, OrganizationAddress organizationAddress,
                                    String websiteUrl, String contactName, String phoneNumber, String emailAddress,
                                    Instant registeredOn) {
        var address = new Address(organizationAddress.getStreet(), organizationAddress.getCity(),
                organizationAddress.getState(), organizationAddress.getCountry(), organizationAddress.getPostalCode());

        save(new PersistableOrganization(id, organizationName, address, websiteUrl, contactName, phoneNumber, emailAddress,
                registeredOn));
    }
}
