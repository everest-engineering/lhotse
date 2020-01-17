package engineering.everest.starterkit.axon.filehandling.filestores;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import engineering.everest.starterkit.axon.filehandling.NativeStorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsS3FileStoreTest {

    @Mock
    private AmazonS3 amazonS3;

    private AwsS3FileStore fileStore;
    private String bucketName = "bucket";

    @BeforeEach
    public void setUp() {
        this.fileStore = new AwsS3FileStore(amazonS3, bucketName);
    }

    @Test
    public void create_CreatesAnObjectInTheS3Bucket() {
        var mockInputStream = mock(InputStream.class);

        var fileIdentifier = fileStore.create(mockInputStream, "fileName");

        assertEquals(fileIdentifier, "s3://bucket/fileName");
        verify(amazonS3).putObject("bucket", "fileName", mockInputStream, null);
    }

    @Test
    public void delete_DeletesTheObjectFromS3Bucket() {
        var fileIdentifier = "s3://bucket/fileName";

        fileStore.delete(fileIdentifier);

        verify(amazonS3).deleteObject("bucket", "fileName");
    }

    @Test
    public void read_FailsIfTheObjectDoesNotExistInTheS3Bucket() {
        var fileIdentifier = "s3://bucket/fileName";

        var exception = assertThrows(RuntimeException.class, () -> fileStore.read(fileIdentifier));
        assertEquals(exception.getMessage(), "Unable to retrieve file: s3://bucket/fileName");
    }

    @Test
    public void read_FetchesTheObjectFromS3Bucket() throws IOException {
        var fileIdentifier = "s3://bucket/fileName";
        var mockS3Object = mock(S3Object.class);
        when(amazonS3.doesObjectExist("bucket", "fileName")).thenReturn(true);
        when(amazonS3.getObject("bucket", "fileName")).thenReturn(mockS3Object);

        fileStore.read(fileIdentifier);

        verify(amazonS3).getObject("bucket", "fileName");
    }

    @Test
    public void nativeStorageTypeIsAwsS3() {
        assertEquals(this.fileStore.nativeStorageType(), NativeStorageType.AWS_S3);
    }
}