package engineering.everest.starterkit.organizations;

import engineering.everest.starterkit.axon.common.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization implements Identifiable {

    private UUID id;
    private String organizationName;
    private OrganizationAddress organizationAddress;
    private String websiteUrl;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;
    private boolean deregistered;

}
