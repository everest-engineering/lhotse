package engineering.everest.lhotse.organizations.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterOrganizationCommand {

    @TargetAggregateIdentifier
    private UUID organizationId;
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
    private String emailAddress;
}
