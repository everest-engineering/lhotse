package engineering.everest.lhotse.axon.filehandling.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileMappingRepository extends MongoRepository<PersistableFileMapping, UUID> {
}
