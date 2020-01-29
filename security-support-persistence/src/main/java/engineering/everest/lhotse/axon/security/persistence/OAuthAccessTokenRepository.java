package engineering.everest.lhotse.axon.security.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OAuthAccessTokenRepository extends MongoRepository<PersistableOAuthAccessToken, UUID> {

    PersistableOAuthAccessToken findByTokenId(String tokenId);

    List<PersistableOAuthAccessToken> findByAuthenticationId(String authenticationId);

    List<PersistableOAuthAccessToken> findByUsernameAndClientId(String username, String clientId);

    List<PersistableOAuthAccessToken> findByClientId(String clientId);

    void deleteByTokenId(String tokenId);

    void deleteByRefreshTokenId(String refreshTokenId);

}
