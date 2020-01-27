package engineering.everest.lhotse.axon.common.services;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadServiceTest {

    @Test
    void getByIdMethodMustExistAndBeGeneric() throws NoSuchMethodException {
        final Method getByIdMethod = ReadService.class.getMethod("getById", UUID.class);
        assertTrue(getByIdMethod.getGenericReturnType() instanceof TypeVariable);
    }
}
