package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String organizationName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String websiteUrl;
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;
    private boolean disabled;
}
