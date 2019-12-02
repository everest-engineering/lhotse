package engineering.everest.starterkit.axon.filehandling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public interface FileService {

    File createTemporaryFile() throws IOException;

    UUID transferToPermanentStore(String originalFilename, InputStream inputStream) throws IOException;

    UUID transferToArtifactStore(String filename, InputStream inputStream) throws IOException;

    UUID transferToArtifactStore(InputStream inputStream) throws IOException;

    InputStream stream(UUID fileId) throws IOException;
}
