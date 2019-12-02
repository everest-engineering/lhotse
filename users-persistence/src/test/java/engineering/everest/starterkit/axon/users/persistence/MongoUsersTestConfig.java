package engineering.everest.starterkit.axon.users.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "engineering.everest.starterkit")
class MongoUsersTestConfig {
}
