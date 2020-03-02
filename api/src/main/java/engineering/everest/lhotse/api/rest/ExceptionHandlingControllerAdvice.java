package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.axon.common.exceptions.RemoteCommandExecutionException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.NoSuchElementException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@ControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler({AggregateNotFoundException.class})
    public void handleNotFoundExceptions(Throwable throwable, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_FOUND, throwable.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public void handleIllegalArguments(Throwable throwable, HttpServletResponse response) throws IOException {
        response.sendError(SC_BAD_REQUEST, throwable.getMessage());
    }

    @ExceptionHandler({RuntimeException.class})
    public void handleRuntimeExceptions(Throwable throwable, HttpServletResponse response) throws IOException {
        response.sendError(SC_BAD_REQUEST, throwable.getMessage());
    }

    @ExceptionHandler({NoSuchElementException.class})
    public void handleNoSuchElementExceptions(Throwable throwable, HttpServletResponse response) throws IOException {
        response.sendError(SC_NOT_FOUND, String.format("Element %s", throwable.getMessage()));
    }

    @ExceptionHandler({ValidationException.class})
    public void handleValidationException(Throwable throwable, HttpServletResponse response) throws IOException {
        response.sendError(SC_BAD_REQUEST, throwable.getMessage());
    }

    @ExceptionHandler({RemoteCommandExecutionException.class})
    public void handleConstraintViolationException(Throwable throwable, HttpServletResponse response) throws IOException {
        Throwable cause = throwable.getCause();
        response.sendError(SC_BAD_REQUEST,
                cause instanceof InterruptedException ? cause.getMessage() : cause.getCause().getMessage());
    }
}
