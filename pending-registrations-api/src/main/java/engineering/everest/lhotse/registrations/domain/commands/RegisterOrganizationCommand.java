package engineering.everest.lhotse.registrations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterOrganizationCommand implements EmailAddressValidatableCommand, UserUniqueEmailValidatableCommand {

    @TargetAggregateIdentifier
    private UUID registrationConfirmationCode;
    private UUID organizationId;
    private UUID registeringUserId;
    private String userEmailAddress;
    private String userEncodedPassword;
    @NotBlank
    private String organizationName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String websiteUrl;
    private String contactName;
    private String phoneNumber;

    @Override
    public String getEmailAddress() {
        return userEmailAddress;
    }
}
