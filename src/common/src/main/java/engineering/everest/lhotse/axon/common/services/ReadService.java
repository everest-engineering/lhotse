package engineering.everest.lhotse.axon.common.services;

import engineering.everest.lhotse.axon.common.domain.Identifiable;

import java.util.UUID;

public interface ReadService<T extends Identifiable> {
    T getById(UUID id);
}
