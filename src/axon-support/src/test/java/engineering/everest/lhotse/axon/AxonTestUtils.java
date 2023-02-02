package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.command.validation.Validates;
import jakarta.validation.Validation;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AxonTestUtils {

    public static CommandValidatingMessageHandlerInterceptor mockCommandValidatingMessageHandlerInterceptor(Validates<
        ?>... mockValidators) {
        var validatorClasses = Arrays.stream(mockValidators).map(e -> e.getClass().getSuperclass()).toList();
        Map<Class<?>, Validates<?>> validatorLookup = new ConcurrentHashMap<>();
        for (int i = 0; i < validatorClasses.size(); i++) {
            Class<?> validator = validatorClasses.get(i);
            Type validatableCommandType = Arrays.stream(validator.getGenericInterfaces())
                .map(e -> (ParameterizedType) e)
                .filter(e -> Validates.class == e.getRawType())
                .map(e -> e.getActualTypeArguments()[0])
                .findFirst().orElseThrow();
            validatorLookup.put((Class<?>) validatableCommandType, mockValidators[i]);
        }

        var commandHandlerInterceptor =
            new CommandValidatingMessageHandlerInterceptor(List.of(), Validation.buildDefaultValidatorFactory().getValidator());
        try {
            var validatorLookupField = commandHandlerInterceptor.getClass().getDeclaredField("validatorLookup");
            validatorLookupField.setAccessible(true);
            validatorLookupField.set(commandHandlerInterceptor, validatorLookup);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return commandHandlerInterceptor;
    }
}
