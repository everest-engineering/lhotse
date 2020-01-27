package engineering.everest.lhotse.axon.filehandling.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import engineering.everest.lhotse.axon.filehandling.FileStore;
import engineering.everest.lhotse.axon.filehandling.filestores.AwsS3FileStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "application.filestore.backend", havingValue = "awsS3")
public class AwsS3FileStoreConfig {

    @Bean
    AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.defaultClient();
    }

    @Bean
    @Qualifier("permanentFileStore")
    FileStore awsS3PermananetFileStore(AmazonS3 s3Client, @Value("${application.filestore.awsS3.buckets.permanent}") String bucketName) {
        return new AwsS3FileStore(s3Client, bucketName);
    }

    @Bean
    @Qualifier("ephemeralFileStore")
    FileStore awsS3EphemeralFileStore(AmazonS3 s3Client, @Value("${application.filestore.awsS3.buckets.ephemeral}") String bucketName) {
        return new AwsS3FileStore(s3Client, bucketName);
    }
}
