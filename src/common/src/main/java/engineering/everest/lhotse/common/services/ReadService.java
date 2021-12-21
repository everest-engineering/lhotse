package engineering.everest.lhotse.common.services;

import engineering.everest.lhotse.common.domain.Identifiable;

import java.util.UUID;

public interface ReadService<T extends Identifiable> {
    T getById(UUID id);
}
