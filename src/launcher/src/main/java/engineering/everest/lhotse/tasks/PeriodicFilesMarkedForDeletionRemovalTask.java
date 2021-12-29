package engineering.everest.lhotse.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@Slf4j
public class PeriodicFilesMarkedForDeletionRemovalTask implements ReplayCompletionAware {

    private final FencedLock singleAppNodeExecutionLock;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;
    private final int batchSize;

    @Autowired
    public PeriodicFilesMarkedForDeletionRemovalTask(HazelcastInstance hazelcastInstance,
                                                     FileService fileService,
                                                     ThumbnailService thumbnailService,
                                                     @Value("${application.filestore.deletion.batch-size:200}") int batchSize) {
        CPSubsystem cpSubsystem = hazelcastInstance.getCPSubsystem();
        this.singleAppNodeExecutionLock = cpSubsystem.getLock(PeriodicFilesMarkedForDeletionRemovalTask.class.getSimpleName());
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedRateString = "PT${storage.files.deletion.fixedRate:5m}")
    @Transactional(propagation = REQUIRES_NEW)
    public void checkForFilesMarkedForDeletionToCleanUp() {
        if (cannotObtainLockOrAnotherInstanceOwnsLock()) {
            return;
        }
        this.deleteFilesInBatches();
    }

    public void deleteFilesInBatches() {
        fileService.deleteEphemeralFileBatch(batchSize);
    }

    private boolean cannotObtainLockOrAnotherInstanceOwnsLock() {
        if (singleAppNodeExecutionLock.isLockedByCurrentThread()) {
            return false;
        }
        if (singleAppNodeExecutionLock.isLocked()) {
            return true;
        }
        return !singleAppNodeExecutionLock.tryLock(5, SECONDS);
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public void replayCompleted() {
        fileService.markAllEphemeralFilesForDeletion();
        thumbnailService.deleteAllThumbnailMappings();
    }
}
