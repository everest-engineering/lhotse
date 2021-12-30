package engineering.everest.lhotse.axon.command;

import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.stereotype.Component;

@Component
public class AxonCommandExecutionExceptionFactory {

    public void throwWrappedInCommandExecutionException(TranslatableException translatableException) {
        throw new CommandExecutionException(translatableException.getMessage(), null, translatableException);
    }
}
