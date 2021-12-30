package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.api.rest.responses.ApiErrorResponse;
import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlingControllerAdviceTest {

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private ExceptionHandlingControllerAdvice controllerAdvice;

    @BeforeEach
    void setUp() {
        controllerAdvice = new ExceptionHandlingControllerAdvice(clock);
    }

    @Test
    void extendsSpringResponseEntityExceptionHandler() {
        assertEquals(controllerAdvice.getClass().getAnnotatedSuperclass().getType(), ResponseEntityExceptionHandler.class);
    }

    @Test
    void willMapAggregateNotFoundExceptions() {
        var exception = new AggregateNotFoundException("aggregate-id", "not found here");
        var expectedResponse = ApiErrorResponse.builder()
            .status(NOT_FOUND)
            .message("not found here")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), NOT_FOUND), controllerAdvice.handleExceptions(exception));
    }

    @Test
    void willMapRuntimeExceptions() {
        var exception = new RuntimeException("kaboom");
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("kaboom")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExceptions(exception));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsNotAnExpectedValidationException() {
        var exception = new ExecutionException("outer exception", new RuntimeException("general case"));
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("outer exception")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExceptions(exception));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsAnIllegalStateException() {
        var exception = new ExecutionException("outer exception", new IllegalStateException("widget disabled"));
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("widget disabled")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExceptions(exception));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsAnIllegalArgumentException() {
        var exception = new ExecutionException("outer exception", new IllegalArgumentException("bad argument"));
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("bad argument")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExceptions(exception));
    }

    @Test
    void willMapMethodArgumentNotValidExceptions() throws Exception {
        var bindingResult = mock(BeanPropertyBindingResult.class);
        var errors = List.of(
            new FieldError("a-name", "first-field-name", "must be provided"),
            new FieldError("another-name", "second-field-name", "not an integer"));
        when(bindingResult.getFieldErrors()).thenReturn(errors);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("first-field-name: must be provided; second-field-name: not an integer")
            .timestamp(Instant.now(clock))
            .build();
        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST),
            controllerAdvice.handleException(ex, mock(WebRequest.class)));
    }

    @Test
    void willMapTranslatableExceptions() {
        var exception = new TranslatableException("EMAIL_ADDRESS_ALREADY_EXISTS");
        var expectedResponse = ApiErrorResponse.builder()
            .status(BAD_REQUEST)
            .message("Email address already exists")
            .timestamp(Instant.now(clock))
            .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExceptions(exception));
    }
}
