package engineering.everest.starterkit.axon.filehandling.persistence;

import engineering.everest.starterkit.axon.filehandling.FileStoreType;
import engineering.everest.starterkit.axon.filehandling.NativeStorageType;
import engineering.everest.starterkit.axon.filehandling.PersistedFileIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fs.mappings")
public class PersistableFileMapping {

    @Id
    private UUID fileId;
    private FileStoreType fileStoreType;
    private NativeStorageType nativeStorageType;
    private String nativeStorageFileId;
    private String sha256;
    private String sha512;
    private Long fileSizeBytes;

    public PersistedFileIdentifier getPersistedFileIdentifier() {
        return new PersistedFileIdentifier(fileId, fileStoreType, nativeStorageType, nativeStorageFileId);
    }
}