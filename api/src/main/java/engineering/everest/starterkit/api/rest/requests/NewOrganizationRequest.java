package engineering.everest.starterkit.api.rest.requests;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NewOrganizationRequest {

    @NotBlank
    @ApiModelProperty(required = true)
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

    public NewOrganizationRequest() {
    }

    public NewOrganizationRequest(@NotBlank String organizationName, String street, String city, String state, String country,
                                  String postalCode, String websiteUrl, String contactName, String contactPhoneNumber,
                                  String contactEmail) {
        this.organizationName = organizationName;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.websiteUrl = websiteUrl;
        this.contactName = contactName;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactEmail = contactEmail;
    }
}