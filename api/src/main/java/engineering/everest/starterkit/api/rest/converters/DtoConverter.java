package engineering.everest.starterkit.api.rest.converters;

import engineering.everest.starterkit.api.rest.responses.OrganizationResponse;
import engineering.everest.starterkit.api.rest.responses.UserResponse;
import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.organizations.Organization;
import engineering.everest.starterkit.organizations.OrganizationAddress;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public UserResponse convert(User user) {
        return new UserResponse(user.getId(), user.getOrganizationId(), user.getUsername(), user.getDisplayName(),
                user.getEmail(), user.isDisabled(), user.getRoles());
    }

    public OrganizationResponse convert(Organization organization) {
        OrganizationAddress organizationAddress = organization.getOrganizationAddress();
        return new OrganizationResponse(organization.getId(), organization.getOrganizationName(),
                organizationAddress.getStreet(),
                organizationAddress.getCity(),
                organizationAddress.getState(),
                organizationAddress.getCountry(),
                organizationAddress.getPostalCode(),
                organization.getWebsiteUrl(),
                organization.getContactName(),
                organization.getPhoneNumber(),
                organization.getEmailAddress(),
                organization.isDeregistered());
    }
}
