package engineering.everest.lhotse.axon.filehandling.filestores;

import engineering.everest.lhotse.axon.filehandling.NativeStorageType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@ExtendWith(MockitoExtension.class)
class MongoGridFsFileStoreTest {

    private MongoGridFsFileStore fileStore;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @BeforeEach
    public void setUp() {
        this.fileStore = new MongoGridFsFileStore(gridFsTemplate);
    }

    @Test
    public void create_StoresFileInGridFs() {
        var objectId = new ObjectId();
        var mockInputStream = mock(InputStream.class);
        when(gridFsTemplate.store(mockInputStream, "file")).thenReturn(objectId);

        assertEquals(fileStore.create(mockInputStream, "file"), objectId.toHexString());
    }

    @Test
    public void delete_RemovesFileInGridFs() {
        String fileIdentifier = "5e253b753496211048764352";
        fileStore.delete(fileIdentifier);

        verify(gridFsTemplate).delete(query(where("_id").is(new ObjectId(fileIdentifier))));
    }

    @Test
    public void read_FailsToFetchFileFromGridFsIfItDoesNotExist() {
        String fileIdentifier = "5e253b753496211048764352";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> fileStore.read(fileIdentifier));

        assertEquals(exception.getMessage(), "Unable to retrieve file 5e253b753496211048764352");
    }

    @Test
    public void nativeStorageTypeIsMongoGridFs() {
        assertEquals(this.fileStore.nativeStorageType(), NativeStorageType.MONGO_GRID_FS);
    }
}