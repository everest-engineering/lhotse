package engineering.everest.lhotse.axon.filehandling.filestores;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import engineering.everest.lhotse.axon.filehandling.FileStore;
import engineering.everest.lhotse.axon.filehandling.NativeStorageType;

import java.io.InputStream;

import static engineering.everest.lhotse.axon.filehandling.NativeStorageType.AWS_S3;

public class AwsS3FileStore implements FileStore {

    private AmazonS3 amazonS3;
    private String bucketName;

    public AwsS3FileStore(AmazonS3 amazonS3, String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    @Override
    public String create(InputStream inputStream, String fileName) {
        amazonS3.putObject(this.bucketName, fileName, inputStream, null);
        return String.format("s3://%s/%s", this.bucketName, fileName);
    }

    @Override
    public void delete(String fileIdentifier) {
        AmazonS3URI s3URI = new AmazonS3URI(fileIdentifier);
        amazonS3.deleteObject(s3URI.getBucket(), s3URI.getKey());
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public InputStream read(String fileIdentifier) {
        AmazonS3URI s3URI = new AmazonS3URI(fileIdentifier);
        if (amazonS3.doesObjectExist(s3URI.getBucket(), s3URI.getKey())) {
            S3Object s3Object = amazonS3.getObject(s3URI.getBucket(), s3URI.getKey());
            return s3Object.getObjectContent();
        } else {
            throw new RuntimeException(String.format("Unable to retrieve file: %s", fileIdentifier));
        }
    }

    @Override
    public NativeStorageType nativeStorageType() {
        return AWS_S3;
    }
}
