package engineering.everest.starterkit.organizations.services;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "engineering.everest.starterkit")
class MongoTestConfig {
}