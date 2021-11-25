package engineering.everest.lhotse.organizations.services;


import java.util.UUID;

public interface OrganizationsService {

    void updateOrganization(UUID requestingUserId, UUID organizationId, String organizationName, String street,
                            String city, String state, String country, String postalCode, String websiteUrl,
                            String contactName, String phoneNumber, String emailAddress);

    void disableOrganization(UUID requestingUserId, UUID organizationId);

    void enableOrganization(UUID requestingUserId, UUID organizationId);
}
