package engineering.everest.starterkit.axon.common.services;

import engineering.everest.starterkit.axon.common.domain.Identifiable;

public interface ReadServiceProvider {
    ReadService<? extends Identifiable> getService(Class<?> clazz);

    ReadService<? extends Identifiable> getService(String classSimpleName);
}
