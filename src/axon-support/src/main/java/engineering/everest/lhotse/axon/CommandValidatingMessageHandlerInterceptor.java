package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

@Component
public class CommandValidatingMessageHandlerInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

    private final Map<Class<?>, Validates<?>> validatorLookup;
    private final Validator javaBeanValidator;

    @Autowired
    @SuppressWarnings("rawtypes")
    public CommandValidatingMessageHandlerInterceptor(List<Validates> validators, Validator javaBeanValidator) {
        this.javaBeanValidator = javaBeanValidator;
        Map<Class<?>, Validates<?>> m = new ConcurrentHashMap<>();
        for (Validates<?> validator : validators) {
            Type validatableCommandType = Arrays.stream(validator.getClass().getGenericInterfaces())
                .map(ParameterizedType.class::cast)
                .filter(e -> Validates.class == e.getRawType())
                .map(e -> e.getActualTypeArguments()[0])
                .findFirst().orElseThrow();
            m.put((Class<?>) validatableCommandType, validator);
        }
        validatorLookup = unmodifiableMap(m);
    }

    @Override
    public Object handle(UnitOfWork<? extends CommandMessage<?>> unitOfWork, InterceptorChain interceptorChain) throws Exception {
        CommandMessage<?> message = unitOfWork.getMessage();
        if (ValidatableCommand.class.isAssignableFrom(message.getPayloadType())) {
            validate(message);
        }
        return interceptorChain.proceed();
    }

    @SuppressWarnings("unchecked")
    private <T extends ValidatableCommand> void validate(CommandMessage<?> commandMessage) {
        var message = (CommandMessage<T>) commandMessage;
        Set<ConstraintViolation<T>> violations = javaBeanValidator.validate(message.getPayload());
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        List<Class<?>> validatableInterfaces = getValidatableInterfaces(commandMessage.getPayloadType());
        for (Class<?> validatableInterface : validatableInterfaces) {
            Validates<?> validator = validatorLookup.get(validatableInterface);
            if (validator == null) {
                continue;
            }
            ((Validates<T>) validator).validate(message.getPayload());
        }
    }

    private List<Class<?>> getValidatableInterfaces(Class<?> commandClass) {
        List<Class<?>> interfaces = Arrays.stream(commandClass.getInterfaces())
            .filter(e -> e != ValidatableCommand.class)
            .filter(ValidatableCommand.class::isAssignableFrom)
            .collect(toList());

        // Add super-interfaces first
        interfaces.addAll(0, interfaces.stream()
            .map(this::getValidatableInterfaces)
            .flatMap(Collection::stream)
            .toList());

        if (commandClass.getSuperclass() != null) {
            interfaces.addAll(0, getValidatableInterfaces(commandClass.getSuperclass()));
        }
        return interfaces;
    }
}
