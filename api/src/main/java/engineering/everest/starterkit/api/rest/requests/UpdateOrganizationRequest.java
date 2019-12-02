package engineering.everest.starterkit.api.rest.requests;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
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

    public UpdateOrganizationRequest() {
    }

    public UpdateOrganizationRequest(String organizationName, String street, String city, String state, String country,
                                     String postalCode, String websiteUrl, String contactName, String phoneNumber,
                                     String emailAddress) {
        this.organizationName = organizationName;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.websiteUrl = websiteUrl;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
}
