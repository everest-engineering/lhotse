package engineering.everest.starterkit.axon.filehandling.config;

import engineering.everest.starterkit.axon.filehandling.DeduplicatingFileStore;
import engineering.everest.starterkit.axon.filehandling.MongoGridFsNativeDeduplicatingFileStore;
import engineering.everest.starterkit.axon.filehandling.persistence.FileMappingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import static engineering.everest.starterkit.axon.filehandling.FileStoreType.ARTIFACT;
import static engineering.everest.starterkit.axon.filehandling.FileStoreType.PERMANENT;

@Configuration
public class FileServiceConfig {

    @Bean
    @Qualifier("permanentFileStore")
    DeduplicatingFileStore permanentFileStore(FileMappingRepository fileMappingRepository,
                                              MongoConverter mongoConverter,
                                              MongoDbFactory dbFactory) {
        GridFsTemplate gridFs = new GridFsTemplate(dbFactory, mongoConverter, "fs.permanent");
        return new MongoGridFsNativeDeduplicatingFileStore(PERMANENT, gridFs, fileMappingRepository);
    }

    @Bean
    @Qualifier("artifactFileStore")
    DeduplicatingFileStore artifactFileStore(FileMappingRepository fileMappingRepository,
                                             MongoConverter mongoConverter,
                                             MongoDbFactory dbFactory) {
        GridFsTemplate gridFs = new GridFsTemplate(dbFactory, mongoConverter, "fs.artifact");
        return new MongoGridFsNativeDeduplicatingFileStore(ARTIFACT, gridFs, fileMappingRepository);
    }
}
