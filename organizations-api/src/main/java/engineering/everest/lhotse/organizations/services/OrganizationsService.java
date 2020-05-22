package engineering.everest.lhotse.organizations.services;


import java.util.UUID;

public interface OrganizationsService {

    UUID registerOrganization(String organizationName, String street, String city, String state, String country,
                              String postalCode, String websiteUrl, String contactName, String contactPhoneNumber,
                              String contactEmailAddress);

    void updateOrganization(UUID requestingUserId, UUID organizationId, String organizationName, String street,
                            String city, String state, String country, String postalCode, String websiteUrl,
                            String contactName, String phoneNumber, String emailAddress);

    UUID createRegisteredOrganization(UUID requestingUserId, String organizationName, String street,
                                      String city, String state, String country, String postalCode, String websiteUrl,
                                      String contactName, String phoneNumber, String emailAddress);

    void disableOrganization(UUID requestingUserId, UUID organizationId);

    void enableOrganization(UUID requestingUserId, UUID organizationId);
}
