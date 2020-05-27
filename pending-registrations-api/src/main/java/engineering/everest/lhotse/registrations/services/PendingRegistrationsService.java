package engineering.everest.lhotse.registrations.services;

import java.util.UUID;

public interface PendingRegistrationsService {

    void registerOrganization(UUID organizationId, UUID userId, String organizationName, String street, String city,
                              String state, String country, String postalCode, String websiteUrl, String contactName,
                              String phoneNumber, String emailAddress, String contactRawPassword);

    void confirmOrganizationRegistrationEmail(UUID organizationId, UUID confirmationCode);
}
