package engineering.everest.lhotse.organizations.services;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "engineering.everest.lhotse")
class MongoTestConfig {
}