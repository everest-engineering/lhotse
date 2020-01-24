package engineering.everest.starterkit.axon.filehandling.config;

import engineering.everest.starterkit.axon.filehandling.FileStore;
import engineering.everest.starterkit.axon.filehandling.filestores.MongoGridFsFileStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
@ConditionalOnProperty(name = "application.filestore.backend", havingValue = "mongoGridFs")
public class MongoGridFsFileStoreConfig {

    @Bean
    @Qualifier("permanentFileStore")
    FileStore mongoGridFsPermanentFileStoreTemplate(MongoConverter mongoConverter,
                                                    MongoDbFactory dbFactory) {
        GridFsTemplate gridFsTemplate = new GridFsTemplate(dbFactory, mongoConverter, "fs.permanent");
        return new MongoGridFsFileStore(gridFsTemplate);
    }

    @Bean
    @Qualifier("ephemeralFileStore")
    FileStore mongoGridFsEphemeralFileStoreTemplate(MongoConverter mongoConverter,
                                                    MongoDbFactory dbFactory) {
        GridFsTemplate gridFsTemplate = new GridFsTemplate(dbFactory, mongoConverter, "fs.ephemeral");
        return new MongoGridFsFileStore(gridFsTemplate);
    }

}
