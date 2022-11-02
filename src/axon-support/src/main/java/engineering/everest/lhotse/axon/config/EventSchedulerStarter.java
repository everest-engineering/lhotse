package engineering.everest.lhotse.axon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventSchedulerStarter {

    @Autowired
    public void configure(org.axonframework.config.Configuration configuration) {
        configuration.eventScheduler(); // Workaround for event scheduler not being registered with Axon lifecycle
    }
}
