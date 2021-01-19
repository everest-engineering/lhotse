package engineering.everest.lhotse.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import engineering.everest.starterkit.filestorage.EphemeralDeduplicatingFileStore;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.filestorage.persistence.PersistableFileMapping;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

@Component
@Log4j2
public class DeletedFileCleanupTask {

    private final FileMappingRepository fileMappingRepository;
    private final EphemeralDeduplicatingFileStore fileStore;
    private final FencedLock singleAppNodeExecutionLock;

    @Autowired
    public DeletedFileCleanupTask(FileMappingRepository fileMappingRepository,
                                  EphemeralDeduplicatingFileStore fileStore,
                                  HazelcastInstance hazelcastInstance) {
        this.fileMappingRepository = fileMappingRepository;
        this.fileStore = fileStore;
        this.singleAppNodeExecutionLock = hazelcastInstance.getCPSubsystem().getLock(DeletedFileCleanupTask.class.getSimpleName());
    }

    @Scheduled(fixedRateString = "PT${storage.files.deletion.fixedRate:5m}")
    void checkForDeletedFilesToCleanUp() {
        if (cannotObtainLockOrAnotherInstanceOwnsLock()) {
            return;
        }
        LOGGER.info("Checking for files to clean up");
        var filesInBatch = fileMappingRepository.findTop500ByMarkedForDeletionTrue().stream()
                .map(PersistableFileMapping::getPersistedFileIdentifier)
                .collect(toSet());
        LOGGER.debug("Cleaning up {} files", filesInBatch.size());
        fileStore.deleteFiles(filesInBatch);
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
}
