package engineering.everest.lhotse.common.services;

import engineering.everest.lhotse.common.domain.Identifiable;

public interface ReadServiceProvider {
    ReadService<? extends Identifiable> getService(Class<?> clazz);

    ReadService<? extends Identifiable> getService(String classSimpleName);
}
