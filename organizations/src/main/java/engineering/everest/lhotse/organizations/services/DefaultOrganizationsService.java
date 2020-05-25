package engineering.everest.lhotse.organizations.services;


import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.organizations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class DefaultOrganizationsService implements OrganizationsService {

    private final RandomFieldsGenerator randomFieldsGenerator;
    private final HazelcastCommandGateway commandGateway;
    private final PasswordEncoder passwordEncoder;

    public DefaultOrganizationsService(RandomFieldsGenerator randomFieldsGenerator,
                                       HazelcastCommandGateway commandGateway, PasswordEncoder passwordEncoder) {
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.commandGateway = commandGateway;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerOrganization(UUID organizationId, UUID userId, String organizationName, String street, String city,
                                     String state, String country, String postalCode, String websiteUrl, String contactName,
                                     String phoneNumber, String emailAddress, String contactRawPassword) {
        commandGateway.sendAndWait(new RegisterOrganizationCommand(organizationId, userId, randomFieldsGenerator.genRandomUUID(),
                emailAddress, encodePasswordIfNotBlank(contactRawPassword), organizationName, street, city, state, country,
                postalCode, websiteUrl, contactName, phoneNumber));
    }

    @Override
    public void confirmOrganizationRegistrationEmail(UUID organizationId, UUID confirmationCode) {
        commandGateway.sendAndWait(new ConfirmOrganizationRegistrationEmailCommand(organizationId, confirmationCode));
    }

    @Override
    public void updateOrganization(UUID requestingUserId, UUID organizationId, String organizationName,
                                   String street, String city, String state, String country, String postalCode,
                                   String websiteUrl, String contactName, String phoneNumber, String emailAddress) {
        commandGateway.sendAndWait(new UpdateOrganizationCommand(organizationId, requestingUserId, organizationName, street, city,
                state, country, postalCode, websiteUrl, contactName, phoneNumber, emailAddress));
    }

    @Override
    public UUID createRegisteredOrganization(UUID requestingUserId, String organizationName, String street, String city, String state,
                                             String country, String postalCode, String websiteUrl, String contactName, String phoneNumber,
                                             String emailAddress) {
        UUID organizationId = randomFieldsGenerator.genRandomUUID();
        return commandGateway.sendAndWait(new CreateRegisteredOrganizationCommand(organizationId,
                requestingUserId, organizationName, street, city, state, country, postalCode, websiteUrl, contactName,
                phoneNumber, emailAddress));
    }

    @Override
    public void disableOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new DisableOrganizationCommand(organizationId, requestingUserId));
    }

    @Override
    public void enableOrganization(UUID requestingUserId, UUID organizationId) {
        commandGateway.sendAndWait(new EnableOrganizationCommand(organizationId, requestingUserId));
    }

    private String encodePasswordIfNotBlank(String passwordChange) {
        return isBlank(passwordChange) ? passwordChange : passwordEncoder.encode(passwordChange);
    }
}
