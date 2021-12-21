package engineering.everest.lhotse.common.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferencableTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Entity implements Referencable {
        private UUID id;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final UUID ID = UUID.randomUUID();
    private static final Entity ENTITY = new Entity(ID);
    private static final String REF_FIELD_STRING = String.format("\"%s:%s\"", Entity.class.getSimpleName(), ID);
    private static final String ENTITY_JSON_STRING = String.format("{\"id\":\"%s\",\"representation\":%s}", ID, REF_FIELD_STRING);

    @Test
    void serializationShouldHaveRefField() throws JsonProcessingException {
        String referenceJsonString = objectMapper.writeValueAsString(ENTITY);
        assertTrue(referenceJsonString.contains(REF_FIELD_STRING));
    }

    @Test
    void deserializationShouldIgnoreRefField() throws IOException {
        Entity entity = objectMapper.readerFor(Entity.class).readValue(ENTITY_JSON_STRING);
        assertEquals(ID, entity.getId());
    }
}
