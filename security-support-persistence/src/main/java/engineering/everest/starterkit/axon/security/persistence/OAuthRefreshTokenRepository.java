package engineering.everest.starterkit.axon.security.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OAuthRefreshTokenRepository extends MongoRepository<PersistableOAuthRefreshToken, UUID> {

    PersistableOAuthRefreshToken findByTokenId(String tokenId);

    void deleteByTokenId(String tokenId);
}
