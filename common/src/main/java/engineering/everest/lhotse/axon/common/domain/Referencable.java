package engineering.everest.lhotse.axon.common.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(value = "representation", allowGetters = true)
public interface Referencable extends Identifiable {
    String SEPARATOR_CHAR = ":";

    @JsonGetter
    default String representation() {
        return toReadableIdentifier(getClass().getSimpleName(), getId());
    }

    static String toReadableIdentifier(String entity, UUID id) {
        return String.format("%s:%s", entity, id);
    }
}
