package engineering.everest.lhotse.organizations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAddress {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
