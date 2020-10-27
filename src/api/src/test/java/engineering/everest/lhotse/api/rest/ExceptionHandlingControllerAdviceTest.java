package engineering.everest.lhotse.api.rest;

import engineering.everest.lhotse.api.rest.responses.ApiErrorResponse;
import engineering.everest.lhotse.axon.common.exceptions.RemoteCommandExecutionException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void annotatedWithExceptionHandlerForAggregateNotFoundException() throws NoSuchMethodException {
        var handleNotFoundException = controllerAdvice.getClass().getMethod("handleNotFoundException", AggregateNotFoundException.class);
        assertDoesNotThrow(() -> handleNotFoundException.getAnnotation(ExceptionHandler.class));
    }

    @Test
    void willMapAggregateNotFoundExceptions() {
        var exception = new AggregateNotFoundException("aggregate-id", "not found here");
        var expectedResponse = ApiErrorResponse.builder()
                .status(NOT_FOUND)
                .message("not found here")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), NOT_FOUND), controllerAdvice.handleNotFoundException(exception));
    }

    @Test
    void annotatedWithExceptionHandlerForRuntimeException() throws NoSuchMethodException {
        var handleRuntimeException = controllerAdvice.getClass().getMethod("handleRuntimeException", RuntimeException.class);
        assertDoesNotThrow(() -> handleRuntimeException.getAnnotation(ExceptionHandler.class));
    }

    @Test
    void willMapRuntimeExceptions() {
        var exception = new RuntimeException("kaboom");
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("kaboom")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleRuntimeException(exception));
    }

    @Test
    void annotatedWithExceptionHandlerForExecutionException() throws NoSuchMethodException {
        Method handleRemoteCommandExecutionException = controllerAdvice.getClass().getMethod("handleExecutionException", ExecutionException.class);
        assertDoesNotThrow(() -> handleRemoteCommandExecutionException.getAnnotation(ExceptionHandler.class));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsNotAnExpectedValidationException() {
        var exception = new ExecutionException("outer exception", new RuntimeException("general case"));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("outer exception")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExecutionException(exception));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsAnIllegalStateException() {
        var exception = new ExecutionException("outer exception", new IllegalStateException("widget disabled"));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("widget disabled")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExecutionException(exception));
    }

    @Test
    void willMapExecutionExceptions_WhenCauseIsAnIllegalArgumentException() {
        var exception = new ExecutionException("outer exception", new IllegalArgumentException("bad argument"));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("bad argument")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleExecutionException(exception));
    }

    @Test
    void annotatedWithExceptionHandlerForRemoteCommandExecutionException() throws NoSuchMethodException {
        Method handleRemoteCommandExecutionException = controllerAdvice.getClass().getMethod("handleRemoteCommandExecutionException", RemoteCommandExecutionException.class);
        assertDoesNotThrow(() -> handleRemoteCommandExecutionException.getAnnotation(ExceptionHandler.class));
    }

    @Test
    void willMapRemoteCommandExecutionExceptionWithNestedExecutionException() {
        var exception = new RemoteCommandExecutionException(new ExecutionException("inner exception", new IllegalArgumentException("bad argument")));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("bad argument")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleRemoteCommandExecutionException(exception));
    }

    @Test
    void willMapRemoteCommandExecutionExceptionWithNestedInterruptedException() {
        var exception = new RemoteCommandExecutionException(new InterruptedException("inner exception"));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("inner exception")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleRemoteCommandExecutionException(exception));
    }

    @Test
    void willMapRemoteCommandExecutionExceptionWithNestedException() {
        var exception = new RemoteCommandExecutionException(new Exception("inner exception"));
        var expectedResponse = ApiErrorResponse.builder()
                .status(BAD_REQUEST)
                .message("inner exception")
                .timeStamp(Instant.now(clock))
                .build();

        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleRemoteCommandExecutionException(exception));
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
                .timeStamp(Instant.now(clock))
                .build();
        assertEquals(new ResponseEntity<>(expectedResponse, new HttpHeaders(), BAD_REQUEST), controllerAdvice.handleException(ex, mock(WebRequest.class)));
    }
}