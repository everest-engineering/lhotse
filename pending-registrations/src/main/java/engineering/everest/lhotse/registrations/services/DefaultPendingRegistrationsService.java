package engineering.everest.lhotse.registrations.services;

import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.registrations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.registrations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class DefaultPendingRegistrationsService implements PendingRegistrationsService {

    private final RandomFieldsGenerator randomFieldsGenerator;
    private final HazelcastCommandGateway commandGateway;
    private final PasswordEncoder passwordEncoder;

    public DefaultPendingRegistrationsService(RandomFieldsGenerator randomFieldsGenerator,
                                              HazelcastCommandGateway commandGateway,
                                              PasswordEncoder passwordEncoder) {
        this.randomFieldsGenerator = randomFieldsGenerator;
        this.commandGateway = commandGateway;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerOrganization(UUID organizationId, UUID userId, String organizationName, String street, String city,
                                     String state, String country, String postalCode, String websiteUrl, String contactName,
                                     String phoneNumber, String emailAddress, String contactRawPassword) {
        commandGateway.sendAndWait(new RegisterOrganizationCommand(randomFieldsGenerator.genRandomUUID(), organizationId, userId,
                emailAddress, encodePasswordIfNotBlank(contactRawPassword), organizationName, street, city, state, country,
                postalCode, websiteUrl, contactName, phoneNumber));
    }

    @Override
    public void confirmOrganizationRegistrationEmail(UUID organizationId, UUID confirmationCode) {
        // This service could verify that a previous requests to register the same organization hasn't been
        // confirmed yet. (It would be a cleaner experience for users). However, let's leave it so that the saga can
        // be used to demonstrate how the process might be modelled.
        commandGateway.sendAndWait(new ConfirmOrganizationRegistrationEmailCommand(confirmationCode, organizationId));
    }

    private String encodePasswordIfNotBlank(String passwordChange) {
        return isBlank(passwordChange) ? passwordChange : passwordEncoder.encode(passwordChange);
    }
}
