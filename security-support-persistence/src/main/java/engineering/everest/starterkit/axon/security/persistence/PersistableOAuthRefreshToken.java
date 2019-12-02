package engineering.everest.starterkit.axon.security.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "oauthRefreshTokens")
public class PersistableOAuthRefreshToken {

    @Id
    private String tokenId;
    private byte[] token;
    private byte[] authentication;
}
