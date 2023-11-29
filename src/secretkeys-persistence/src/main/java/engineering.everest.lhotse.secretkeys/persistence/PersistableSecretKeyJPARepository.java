package engineering.everest.lhotse.secretkeys.persistence;

import engineering.everest.axon.cryptoshredding.TypeDifferentiatedSecretKeyId;
import engineering.everest.axon.cryptoshredding.persistence.PersistableSecretKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersistableSecretKeyJPARepository extends JpaRepository<PersistableSecretKey, TypeDifferentiatedSecretKeyId> {}
