package engineering.everest.lhotse.api.rest.converters;

import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.organizations.Organization;
import org.springframework.stereotype.Service;

@Service
public class DtoConverter {

    public UserResponse convert(User user) {
        return new UserResponse(user.getId(), user.getOrganizationId(), user.getUsername(), user.getDisplayName(),
            user.getEmail(), user.isDisabled());
    }

    public OrganizationResponse convert(Organization organization) {
        var address = organization.getOrganizationAddress();
        return new OrganizationResponse(organization.getId(), organization.getOrganizationName(),
            address.getStreet(),
            address.getCity(),
            address.getState(),
            address.getCountry(),
            address.getPostalCode(),
            organization.getWebsiteUrl(),
            organization.getContactName(),
            organization.getPhoneNumber(),
            organization.getEmailAddress(),
            organization.isDisabled());
    }
}
