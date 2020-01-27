package engineering.everest.lhotse.axon.filehandling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistedFileIdentifier implements Serializable {

    private UUID fileId;
    private FileStoreType fileStoreType;
    private NativeStorageType storageType;
    private String nativeStorageFileId;

}
