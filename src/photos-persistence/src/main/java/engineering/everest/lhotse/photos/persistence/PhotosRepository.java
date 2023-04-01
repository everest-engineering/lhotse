package engineering.everest.lhotse.photos.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotosRepository extends JpaRepository<PersistablePhoto, UUID> {

    default void createPhoto(UUID id, UUID ownerUserId, UUID persistedFileId, String filename, Instant uploadTimestamp) {
        save(new PersistablePhoto(id, ownerUserId, persistedFileId, filename, uploadTimestamp));
    }

    List<PersistablePhoto> findByOwnerUserId(UUID ownerId, Pageable pageable);

    Optional<PersistablePhoto> findByIdAndOwnerUserId(UUID photoId, UUID ownerUserId);
}
