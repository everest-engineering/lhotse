package engineering.everest.starterkit.axon.common.services;

import engineering.everest.starterkit.axon.common.domain.Identifiable;

import java.util.UUID;

public interface ReadService<T extends Identifiable> {
    T getById(UUID id);
}
