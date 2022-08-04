package engineering.everest.lhotse.axon.command.validation;

import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import org.axonframework.commandhandling.CommandExecutionException;

public interface Validates<T extends ValidatableCommand> {

    void validate(T validatable);

    default void throwWrappedInCommandExecutionException(TranslatableException translatableException) {
        throw new CommandExecutionException(translatableException.getMessage(), null, translatableException);
    }
}
