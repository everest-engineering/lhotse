package engineering.everest.lhotse.axon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import engineering.everest.axon.cryptoshredding.CryptoShreddingKeyService;
import engineering.everest.axon.cryptoshredding.encryption.EncrypterDecrypterFactory;
import engineering.everest.axon.cryptoshredding.serialization.CryptoShreddingSerializer;
import engineering.everest.axon.cryptoshredding.serialization.DefaultValueProvider;
import engineering.everest.axon.cryptoshredding.serialization.KeyIdentifierToStringConverter;
import engineering.everest.lhotse.axon.replay.ReplayMarkerAwareTrackingEventProcessorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.lang.management.ManagementFactory;

@Slf4j
@Configuration
public class AxonConfig {

    @Autowired
    public void configure(TaskExecutor taskExecutor,
                          EventProcessingModule eventProcessingModule) {
        eventProcessingModule.registerEventProcessorFactory(
            new ReplayMarkerAwareTrackingEventProcessorBuilder(taskExecutor, eventProcessingModule));
    }

    @Bean
    public CryptoShreddingSerializer eventSerializer(CryptoShreddingKeyService cryptoShreddingKeyService,
                                                     EncrypterDecrypterFactory aesEncrypterDecrypterFactory,
                                                     DefaultValueProvider defaultValueProvider,
                                                     KeyIdentifierToStringConverter keyIdentifierToStringConverter) {
        return new CryptoShreddingSerializer(JacksonSerializer.defaultSerializer(), cryptoShreddingKeyService,
            aesEncrypterDecrypterFactory, new ObjectMapper(), defaultValueProvider, keyIdentifierToStringConverter);
    }

    @Bean
    @SuppressWarnings("MethodName")
    public XStream xStream() {
        @SuppressWarnings("LocalVariableName")
        var xStream = new XStream();

        xStream.allowTypesByWildcard(new String[] {
            "engineering.everest.**",
            "java.util.**"
        });
        return xStream;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public SagaStore globalSagaStore(EntityManagerProvider entityManagerProvider,
                                     CryptoShreddingSerializer cryptoShreddingEventSerializer) {
        return JpaSagaStore.builder()
            .serializer(cryptoShreddingEventSerializer)
            .entityManagerProvider(entityManagerProvider)
            .build();
    }

    @Bean
    public TokenStore tokenStore(EntityManagerProvider entityManagerProvider, Serializer defaultSerializer) {
        return JpaTokenStore.builder()
            .entityManagerProvider(entityManagerProvider)
            .serializer(defaultSerializer)
            .nodeId(ManagementFactory.getRuntimeMXBean().getName())
            .build();
    }
}
