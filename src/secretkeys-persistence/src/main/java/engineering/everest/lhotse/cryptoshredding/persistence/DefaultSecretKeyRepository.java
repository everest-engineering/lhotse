package engineering.everest.lhotse.cryptoshredding.persistence;

import engineering.everest.axon.cryptoshredding.TypeDifferentiatedSecretKeyId;
import engineering.everest.axon.cryptoshredding.persistence.PersistableSecretKey;
import engineering.everest.axon.cryptoshredding.persistence.SecretKeyRepository;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.util.Optional;

@Repository
public class DefaultSecretKeyRepository implements SecretKeyRepository {

    final PersistableSecretKeyJPARepository repository;

    public DefaultSecretKeyRepository(PersistableSecretKeyJPARepository repository) {
        this.repository = repository;
    }

    @Override
    public PersistableSecretKey create(TypeDifferentiatedSecretKeyId keyId, SecretKey key) {
        return repository.save(new PersistableSecretKey(keyId, key.getEncoded(), key.getAlgorithm()));
    }

    @Override
    public Optional<PersistableSecretKey> findById(TypeDifferentiatedSecretKeyId keyId) {
        return repository.findById(keyId);
    }

    @Override
    public PersistableSecretKey save(PersistableSecretKey key) {
        return repository.save(key);
    }
}
