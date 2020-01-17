package engineering.everest.starterkit.axon.filehandling;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.CountingInputStream;
import engineering.everest.starterkit.axon.filehandling.persistence.FileMappingRepository;
import engineering.everest.starterkit.axon.filehandling.persistence.PersistableFileMapping;
import org.springframework.data.domain.Example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.UUID.randomUUID;

public class NativeDeduplicatingFileStore implements DeduplicatingFileStore {

    private final FileStoreType fileStoreType;
    private final FileMappingRepository fileMappingRepository;
    private FileStore fileStore;

    public NativeDeduplicatingFileStore(FileStoreType fileStoreType,
                                        FileMappingRepository fileMappingRepository,
                                        FileStore fileStore) {
        this.fileStoreType = fileStoreType;
        this.fileMappingRepository = fileMappingRepository;
        this.fileStore = fileStore;
    }

    @Override
    public PersistedFile store(String originalFilename, InputStream inputStream) throws IOException {
        try (var countingInputStream = new CountingInputStream(inputStream);
             var sha256ingInputStream = new HashingInputStream(Hashing.sha256(), countingInputStream);
             var sha512ingInputStream = new HashingInputStream(Hashing.sha512(), sha256ingInputStream)) {
            String fileIdentifier = fileStore.create(sha512ingInputStream, originalFilename);
            long fileSizeBytes = countingInputStream.getCount();
            PersistedFile persistedFile = deduplicateUploadedFile(fileIdentifier,
                    sha256ingInputStream.hash().toString(), sha512ingInputStream.hash().toString(),
                    fileSizeBytes, fileStore.nativeStorageType());
            updateFileMapping(persistedFile, fileSizeBytes, fileStore.nativeStorageType());
            return persistedFile;
        }
    }

    @Override
    public InputStream stream(PersistedFileIdentifier persistedFileIdentifier) throws IOException {
        return fileStore.read(persistedFileIdentifier.getNativeStorageFileId());
    }

    private PersistedFile deduplicateUploadedFile(String fileIdentifier, String uploadSha256, String uploadSha512,
                                                  long fileSizeBytes, NativeStorageType nativeStorageType) {
        Optional<PersistableFileMapping> existingFileMapping = searchForExistingFileMappingToBothHashes(uploadSha256, uploadSha512);

        if (existingFileMapping.isPresent()) {
            fileStore.delete(fileIdentifier);
            return new PersistedFile(randomUUID(), fileStoreType, nativeStorageType, existingFileMapping.get().getNativeStorageFileId(),
                    uploadSha256, uploadSha512, fileSizeBytes);
        } else {
            return new PersistedFile(randomUUID(), fileStoreType, nativeStorageType, fileIdentifier,
                    uploadSha256, uploadSha512, fileSizeBytes);
        }
    }

    private Optional<PersistableFileMapping> searchForExistingFileMappingToBothHashes(String uploadSha256, String uploadSha512) {
        var fileMappingExample = new PersistableFileMapping();
        fileMappingExample.setSha256(uploadSha256);
        fileMappingExample.setSha512(uploadSha512);
        return fileMappingRepository.findOne(Example.of(fileMappingExample));
    }

    private void updateFileMapping(PersistedFile persistedFile, long fileSizeBytes, NativeStorageType nativeStorageType) {
        fileMappingRepository.save(new PersistableFileMapping(persistedFile.getFileId(), fileStoreType, nativeStorageType,
                persistedFile.getNativeStorageFileId(), persistedFile.getSha256(), persistedFile.getSha512(), fileSizeBytes));
    }
}
