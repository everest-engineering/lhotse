package engineering.everest.lhotse.users.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "engineering.everest.lhotse")
class MongoUsersTestConfig {
}
