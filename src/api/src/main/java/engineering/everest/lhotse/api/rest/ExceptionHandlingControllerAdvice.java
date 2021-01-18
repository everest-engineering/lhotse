package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.api.rest.responses.ApiErrorResponse;
import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import engineering.everest.starterkit.axon.exceptions.RemoteCommandExecutionException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class ExceptionHandlingControllerAdvice extends ResponseEntityExceptionHandler {

    private final Clock clock;

    public ExceptionHandlingControllerAdvice(Clock clock) {
        super();
        this.clock = clock;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleExceptions(Exception exception) {
        if (TranslatableException.class.isAssignableFrom(exception.getClass())) {
            return handleTranslatableException((TranslatableException) exception);
        }
        if (exception instanceof AggregateNotFoundException) {
            return handleNotFoundException((AggregateNotFoundException) exception);
        }
        if (exception instanceof ExecutionException) {
            return handleExecutionException((ExecutionException) exception);
        }
        if (exception instanceof RemoteCommandExecutionException) {
            return handleRemoteCommandExecutionException((RemoteCommandExecutionException) exception);
        }

        return handleGenericException(exception);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
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

    private ResponseEntity<Object> handleRemoteCommandExecutionException(RemoteCommandExecutionException exception) {
        var cause = exception.getCause();
        if (cause instanceof ExecutionException) {
            return handleExecutionException((ExecutionException) cause);
        }
        return new ResponseEntity<>(createResponseBody(cause.getMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ResponseEntity<Object> handleGenericException(Exception exception) {
        return new ResponseEntity<>(createResponseBody(exception.getMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    private ApiErrorResponse createResponseBody(String message, HttpStatus badRequest) {
        return ApiErrorResponse.builder()
                .status(badRequest)
                .message(message)
                .timeStamp(Instant.now(clock))
                .build();
    }
}
