package engineering.everest.lhotse.api.rest.requests;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOrganizationRequest {
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
        @NotBlank
        private String contactEmail;
}
