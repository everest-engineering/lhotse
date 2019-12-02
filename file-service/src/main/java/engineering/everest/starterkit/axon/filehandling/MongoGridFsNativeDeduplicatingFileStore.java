package engineering.everest.starterkit.axon.filehandling;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.CountingInputStream;
import engineering.everest.starterkit.axon.filehandling.persistence.PersistableFileMapping;
import engineering.everest.starterkit.axon.filehandling.persistence.FileMappingRepository;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static engineering.everest.starterkit.axon.filehandling.NativeStorageType.MONGO_GRID_FS;
import static java.util.UUID.randomUUID;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoGridFsNativeDeduplicatingFileStore implements DeduplicatingFileStore {

    private final FileStoreType fileStoreType;
    private final GridFsTemplate gridFs;
    private final FileMappingRepository fileMappingRepository;

    public MongoGridFsNativeDeduplicatingFileStore(FileStoreType fileStoreType,
                                                   GridFsTemplate gridFs,
                                                   FileMappingRepository fileMappingRepository) {
        this.fileStoreType = fileStoreType;
        this.gridFs = gridFs;
        this.fileMappingRepository = fileMappingRepository;
    }

    @Override
    public PersistedFile store(String originalFilename, InputStream inputStream) throws IOException {
        try (var countingInputStream = new CountingInputStream(inputStream);
             var sha256ingInputStream = new HashingInputStream(Hashing.sha256(), countingInputStream);
             var sha512ingInputStream = new HashingInputStream(Hashing.sha512(), sha256ingInputStream)) {
            ObjectId mongoObjectId = gridFs.store(sha512ingInputStream, originalFilename);
            long fileSizeBytes = countingInputStream.getCount();
            PersistedFile persistedFile = deduplicateUploadedFile(mongoObjectId,
                    sha256ingInputStream.hash().toString(), sha512ingInputStream.hash().toString(), fileSizeBytes);
            updateFileMapping(persistedFile, fileSizeBytes);
            return persistedFile;
        }
    }

    @Override
    public InputStream stream(PersistedFileIdentifier persistedFileIdentifier) throws IOException {
        var gridFSFile = gridFs.findOne(new Query(where("_id").is(persistedFileIdentifier.getNativeStorageFileId())));
        if (gridFSFile == null) {
            throw new RuntimeException("Unable to retrieve file " + persistedFileIdentifier);
        }
        return gridFs.getResource(gridFSFile).getInputStream();
    }

    private PersistedFile deduplicateUploadedFile(ObjectId newMongoObjectId, String uploadSha256, String uploadSha512, long fileSizeBytes) {
        Optional<PersistableFileMapping> existingFileMapping = searchForExistingFileMappingToBothHashes(uploadSha256, uploadSha512);

        if (existingFileMapping.isPresent()) {
            gridFs.delete(query(where("_id").is(newMongoObjectId)));
            return new PersistedFile(randomUUID(), fileStoreType, MONGO_GRID_FS, existingFileMapping.get().getNativeStorageFileId(),
                    uploadSha256, uploadSha512, fileSizeBytes);
        } else {
            return new PersistedFile(randomUUID(), fileStoreType, MONGO_GRID_FS, newMongoObjectId.toString(),
                    uploadSha256, uploadSha512, fileSizeBytes);
        }
    }

    private Optional<PersistableFileMapping> searchForExistingFileMappingToBothHashes(String uploadSha256, String uploadSha512) {
        var fileMappingExample = new PersistableFileMapping();
        fileMappingExample.setSha256(uploadSha256);
        fileMappingExample.setSha512(uploadSha512);
        return fileMappingRepository.findOne(Example.of(fileMappingExample));
    }

    private void updateFileMapping(PersistedFile persistedFile, long fileSizeBytes) {
        fileMappingRepository.save(new PersistableFileMapping(persistedFile.getFileId(), fileStoreType, MONGO_GRID_FS,
                persistedFile.getNativeStorageFileId(), persistedFile.getSha256(), persistedFile.getSha512(), fileSizeBytes));
    }
}
