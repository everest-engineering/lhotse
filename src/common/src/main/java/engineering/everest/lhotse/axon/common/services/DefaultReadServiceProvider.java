package engineering.everest.lhotse.axon.common.services;

import engineering.everest.lhotse.axon.common.domain.Identifiable;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class DefaultReadServiceProvider implements ReadServiceProvider {

    private static final String GET_BY_ID_METHOD = "getById";

    private final Map<String, ReadService<? extends Identifiable>> readerServicesLookup;

    @Autowired
    public DefaultReadServiceProvider(List<ReadService<? extends Identifiable>> readServices) throws NoSuchMethodException {
        readerServicesLookup = new ConcurrentHashMap<>();

        for (ReadService<? extends Identifiable> readService : readServices) {
            readerServicesLookup.put(
                    readService.getClass().getMethod(GET_BY_ID_METHOD, UUID.class).getReturnType().getSimpleName(),
                    readService);
        }
        LOGGER.info("Read Services loaded ({}): {}", readerServicesLookup.size(), readerServicesLookup);
    }

    @Override
    public ReadService<? extends Identifiable> getService(Class<?> clazz) {
        return getService(clazz.getSimpleName());
    }

    @Override
    public ReadService<? extends Identifiable> getService(String classSimpleName) {
        return readerServicesLookup.get(classSimpleName);
    }
}
