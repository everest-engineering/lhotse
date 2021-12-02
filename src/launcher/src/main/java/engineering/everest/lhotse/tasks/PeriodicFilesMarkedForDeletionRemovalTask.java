package engineering.everest.lhotse.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.starterkit.filestorage.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
@Slf4j
public class PeriodicFilesMarkedForDeletionRemovalTask implements ReplayCompletionAware {

    private final FencedLock singleAppNodeExecutionLock;
    private final FileService fileService;
    private final int batchSize;

    @Autowired
    public PeriodicFilesMarkedForDeletionRemovalTask(HazelcastInstance hazelcastInstance,
                                                     FileService fileService,
                                                     @Value("${application.filestore.deletion.batch-size:200}") int batchSize) {
        CPSubsystem cpSubsystem = hazelcastInstance.getCPSubsystem();
        this.singleAppNodeExecutionLock = cpSubsystem.getLock(PeriodicFilesMarkedForDeletionRemovalTask.class.getSimpleName());
        this.fileService = fileService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedRateString = "PT${storage.files.deletion.fixedRate:5m}")
    public void checkForFilesMarkedForDeletionToCleanUp() {
        if (cannotObtainLockOrAnotherInstanceOwnsLock()) {
            return;
        }
        this.deleteFilesInBatches();
    }

    public void deleteFilesInBatches() {
        fileService.deleteFileBatch(batchSize);
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
    public void replayCompleted() {
        fileService.markAllFilesForDeletion();
    }
}
