package engineering.everest.starterkit.organizations.services;


import java.util.UUID;

public interface OrganizationsService {

    UUID createOrganization(UUID requestingUserId, String organizationName, String street,
                            String city, String state, String country, String postalCode, String websiteUrl,
                            String contactName, String phoneNumber, String emailAddress);

    void updateOrganization(UUID requestingUserId, UUID organizationId, String organizationName, String street,
                            String city, String state, String country, String postalCode,
                            String websiteUrl, String contactName, String phoneNumber, String emailAddress);

    void deregisterOrganization(UUID requestingUserId, UUID organizationId);

    void reregisterOrganization(UUID requestingUserId, UUID organizationId);
}
