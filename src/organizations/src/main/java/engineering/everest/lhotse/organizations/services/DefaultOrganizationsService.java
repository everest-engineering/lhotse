package engineering.everest.lhotse.organizations.services;

import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultOrganizationsService implements OrganizationsService {

    private final CommandGateway commandGateway;

    public DefaultOrganizationsService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public void updateOrganization(UUID requestingUserId,
                                   UUID organizationId,
                                   String organizationName,
                                   String street,
                                   String city,
                                   String state,
                                   String country,
                                   String postalCode,
                                   String websiteUrl,
                                   String contactName,
                                   String phoneNumber,
                                   String emailAddress) {
        commandGateway.sendAndWait(new UpdateOrganizationCommand(organizationId, requestingUserId, organizationName, street, city,
            state, country, postalCode, websiteUrl, contactName, phoneNumber, emailAddress));
    }

    @Override
    public void disableOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new DisableOrganizationCommand(organizationId, requestingUserId));
    }

    @Override
    public void enableOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new EnableOrganizationCommand(organizationId, requestingUserId));
    }
}
