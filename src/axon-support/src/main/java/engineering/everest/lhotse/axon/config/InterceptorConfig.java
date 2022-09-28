package engineering.everest.lhotse.axon.config;

import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.axon.LoggingMessageHandlerInterceptor;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.spring.config.SpringAxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfig {

    @Autowired
    public void registerInterceptors(@Autowired CommandBus commandBus,
                                     @Autowired SpringAxonConfiguration axonConfiguration,
                                     @Autowired CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor,
                                     @Autowired LoggingMessageHandlerInterceptor loggingMessageHandlerInterceptor) {
        commandBus.registerHandlerInterceptor(new CorrelationDataInterceptor<>(axonConfiguration.getObject().correlationDataProviders()));
        commandBus.registerHandlerInterceptor(commandValidatingMessageHandlerInterceptor);
        commandBus.registerHandlerInterceptor(loggingMessageHandlerInterceptor);
    }
}
