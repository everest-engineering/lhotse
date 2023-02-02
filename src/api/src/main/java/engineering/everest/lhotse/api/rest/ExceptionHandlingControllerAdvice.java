package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.api.rest.responses.ApiErrorResponse;
import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingControllerAdvice extends ResponseEntityExceptionHandler {

    private final Clock clock;

    public ExceptionHandlingControllerAdvice(Clock clock) {
        super();
        this.clock = clock;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleExceptions(Exception exception) {
        LOGGER.debug("Mapping exception:", exception);

        if (exception instanceof TranslatableException e) {
            return handleTranslatableException(e);
        }
        if (exception instanceof AggregateNotFoundException e) {
            return handleNotFoundException(e);
        }
        if (exception instanceof ExecutionException e) {
            return handleExecutionException(e);
        }
        if (exception instanceof CommandExecutionException e) {
            return handleCommandExecutionException(e);
        }
        return handleGenericException(exception);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
            .map(x -> String.format("%s: %s", x.getField(), x.getDefaultMessage()))
            .collect(joining("; "));
        return new ResponseEntity<>(createResponseBody(errors, BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ResponseEntity<Object> handleTranslatableException(TranslatableException exception) {
        return new ResponseEntity<>(createResponseBody(exception.getLocalizedMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ResponseEntity<Object> handleNotFoundException(AggregateNotFoundException exception) {
        return new ResponseEntity<>(createResponseBody(exception.getMessage(), NOT_FOUND), new HttpHeaders(), NOT_FOUND);
    }

    private ResponseEntity<Object> handleExecutionException(ExecutionException exception) {
        var cause = exception.getCause();
        String message = exception.getMessage();
        if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException) {
            message = cause.getMessage();
        } else if (cause.getCause() instanceof TranslatableException) {
            message = cause.getCause().getLocalizedMessage();
        }
        return new ResponseEntity<>(createResponseBody(message, BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ResponseEntity<Object> handleCommandExecutionException(CommandExecutionException exception) {
        String message = exception.getMessage();
        if (exception.getDetails().isPresent()) {
            var details = exception.getDetails().orElseThrow();
            if (details instanceof TranslatableException e) {
                message = e.getLocalizedMessage();
            }
        }
        return new ResponseEntity<>(createResponseBody(message, BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ResponseEntity<Object> handleGenericException(Exception exception) {
        return new ResponseEntity<>(createResponseBody(exception.getMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ApiErrorResponse createResponseBody(String message, HttpStatus httpStatus) {
        return ApiErrorResponse.builder()
            .status(httpStatus)
            .message(message)
            .timestamp(Instant.now(clock))
            .build();
    }
}
