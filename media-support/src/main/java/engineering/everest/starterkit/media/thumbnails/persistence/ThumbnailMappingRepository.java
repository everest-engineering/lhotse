package engineering.everest.starterkit.media.thumbnails.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThumbnailMappingRepository extends MongoRepository<PersistableThumbnailMapping, UUID> {

}
