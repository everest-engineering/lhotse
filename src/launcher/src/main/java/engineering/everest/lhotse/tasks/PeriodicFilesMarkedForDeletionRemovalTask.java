package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@Slf4j
public class PeriodicFilesMarkedForDeletionRemovalTask implements ReplayCompletionAware {
    private static final String POSTGRES_TRY_LOCK = "SELECT pg_try_advisory_lock(42)";
    private static final String POSTGRES_UNLOCK = "SELECT pg_advisory_unlock(42)";

    private final EntityManager entityManager;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;
    private final int batchSize;

    @Autowired
    public PeriodicFilesMarkedForDeletionRemovalTask(@Qualifier("sharedEntityManager") EntityManager entityManager,
                                                     FileService fileService,
                                                     ThumbnailService thumbnailService,
                                                     @Value("${application.filestore.deletion.batch-size:400}") int batchSize) {
        this.entityManager = entityManager;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedRateString = "PT${storage.files.deletion.fixedRate:5m}")
    @Transactional(propagation = REQUIRES_NEW)
    public void checkForFilesMarkedForDeletionToCleanUp() {
        if (!(boolean) entityManager.createNativeQuery(POSTGRES_TRY_LOCK).getSingleResult()) {
            LOGGER.info("Locked by another process");
            return;
        }

        LOGGER.info("Lock acquired");
        this.deleteFilesInBatches();
        entityManager.createNativeQuery(POSTGRES_UNLOCK).getSingleResult();
        LOGGER.info("Lock released");
    }

    public void deleteFilesInBatches() {
        fileService.deleteEphemeralFileBatch(batchSize);
    }

    @Override
    public void replayCompleted() {
        fileService.markAllEphemeralFilesForDeletion();
        thumbnailService.deleteAllThumbnailMappings();
    }
}
