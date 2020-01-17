package engineering.everest.starterkit.axon.filehandling.config;

import engineering.everest.starterkit.axon.filehandling.DeduplicatingFileStore;
import engineering.everest.starterkit.axon.filehandling.FileStore;
import engineering.everest.starterkit.axon.filehandling.NativeDeduplicatingFileStore;
import engineering.everest.starterkit.axon.filehandling.persistence.FileMappingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static engineering.everest.starterkit.axon.filehandling.FileStoreType.EPHEMERAL;
import static engineering.everest.starterkit.axon.filehandling.FileStoreType.PERMANENT;

@Configuration
public class DeduplicatingFileStoreConfig {

    @Bean
    @Qualifier("permanentDeduplicatingFileStore")
    DeduplicatingFileStore permanentFileStore(FileMappingRepository fileMappingRepository,
                                              @Qualifier("permanentFileStore") FileStore fileStore) {
        return new NativeDeduplicatingFileStore(PERMANENT, fileMappingRepository, fileStore);
    }

    @Bean
    @Qualifier("ephemeralDeduplicatingFileStore")
    DeduplicatingFileStore ephemeralFileStore(FileMappingRepository fileMappingRepository,
                                              @Qualifier("ephemeralFileStore") FileStore fileStore) {
        return new NativeDeduplicatingFileStore(EPHEMERAL, fileMappingRepository, fileStore);
    }
}
