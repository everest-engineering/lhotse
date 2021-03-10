package engineering.everest.lhotse.api.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {
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
