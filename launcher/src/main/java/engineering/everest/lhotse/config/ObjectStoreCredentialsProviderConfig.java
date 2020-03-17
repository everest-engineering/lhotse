package engineering.everest.lhotse.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStoreCredentialsProviderConfig {

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider(
            @Value("${application.filestore.awsS3.accessKeyId:}") String awsAccessKeyId,
            @Value("${application.filestore.awsS3.secretKey:}") String awsSecretKey) {
        if (!awsAccessKeyId.isBlank() || !awsSecretKey.isBlank()) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey));
        }
        return DefaultAWSCredentialsProviderChain.getInstance();
    }
}
