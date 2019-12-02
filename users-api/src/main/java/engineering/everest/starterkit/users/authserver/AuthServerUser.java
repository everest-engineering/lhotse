package engineering.everest.starterkit.users.authserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthServerUser {

    private String username;
    private String encodedPassword;
    private boolean disabled;
}
