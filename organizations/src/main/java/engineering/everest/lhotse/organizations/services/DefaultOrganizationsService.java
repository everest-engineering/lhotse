package engineering.everest.lhotse.organizations.services;


import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.organizations.domain.commands.DeregisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.ReregisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultOrganizationsService implements OrganizationsService {

    private final RandomFieldsGenerator randomFieldsGenerator;
    private final HazelcastCommandGateway commandGateway;

    public DefaultOrganizationsService(RandomFieldsGenerator randomFieldsGenerator,
                                       HazelcastCommandGateway commandGateway) {
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.commandGateway = commandGateway;
    }

    @Override
    public UUID createOrganization(UUID requestingUserId, String organizationName, String street, String city, String state,
                                   String country, String postalCode, String websiteUrl, String contactName, String phoneNumber,
                                   String emailAddress) {
        UUID organizationId = randomFieldsGenerator.genRandomUUID();
        return commandGateway.sendAndWait(new RegisterOrganizationCommand(organizationId,
                requestingUserId, organizationName, street, city, state, country, postalCode, websiteUrl, contactName,
                phoneNumber, emailAddress));
    }

    @Override
    public void updateOrganization(UUID requestingUserId, UUID organizationId, String organizationName,
                                   String street, String city, String state, String country, String postalCode,
                                   String websiteUrl, String contactName, String phoneNumber, String emailAddress) {
        commandGateway.sendAndWait(new UpdateOrganizationCommand(organizationId, requestingUserId, organizationName, street, city,
                state, country, postalCode, websiteUrl, contactName, phoneNumber, emailAddress));
    }

    @Override
    public void deregisterOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new DeregisterOrganizationCommand(organizationId, requestingUserId));
    }

    @Override
    public void reregisterOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new ReregisterOrganizationCommand(organizationId, requestingUserId));
    }
}
