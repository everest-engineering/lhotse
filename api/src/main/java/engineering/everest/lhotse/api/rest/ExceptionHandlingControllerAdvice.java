package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.api.rest.responses.ApiErrorResponse;
import engineering.everest.lhotse.axon.common.exceptions.RemoteCommandExecutionException;
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

    @ExceptionHandler(AggregateNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(AggregateNotFoundException exception) {
        return new ResponseEntity<>(createResponseBody(exception.getMessage(), NOT_FOUND), new HttpHeaders(), NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exception) {
        return new ResponseEntity<>(createResponseBody(exception.getMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Object> handleExecutionException(ExecutionException exception) {
        var cause = exception.getCause();
        var message = cause instanceof IllegalArgumentException || cause instanceof IllegalStateException
                ? cause.getMessage()
                : exception.getMessage();
        return new ResponseEntity<>(createResponseBody(message, BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(RemoteCommandExecutionException.class)
    public ResponseEntity<Object> handleRemoteCommandExecutionException(RemoteCommandExecutionException exception) {
        var cause = exception.getCause();
        if (cause instanceof ExecutionException) {
            return handleExecutionException((ExecutionException)cause);
        }
        return new ResponseEntity<>(createResponseBody(cause.getMessage(), BAD_REQUEST), new HttpHeaders(), BAD_REQUEST);
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

    private ApiErrorResponse createResponseBody(String message, HttpStatus badRequest) {
        return ApiErrorResponse.builder()
                .status(badRequest)
                .message(message)
                .timeStamp(Instant.now(clock))
                .build();
    }
}
