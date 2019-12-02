package engineering.everest.starterkit.axon;

import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.axonframework.modelling.command.VersionedAggregateIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static engineering.everest.starterkit.axon.config.AxonHazelcastConfig.AXON_COMMAND_DISPATCHER;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HazelcastCommandGatewayTest {

    private static final String AGGREGATE_IDENTIFIER = "aggregate-identifier";
    private static final long AGGREGATE_VERSION = 1234L;
    private static final VersionedAggregateIdentifier VERSIONED_AGGREGATE_IDENTIFIER = new VersionedAggregateIdentifier(AGGREGATE_IDENTIFIER, AGGREGATE_VERSION);

    private HazelcastCommandGateway hazelcastCommandGateway;

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private MessageDispatchInterceptor<? super CommandMessage<?>> messageDispatchInterceptor;
    @Mock
    private IExecutorService executorService;
    @Mock
    private AnnotationCommandTargetResolver annotationCommandTargetResolver;
    @Mock
    private CommandMessage<?> command;

    @BeforeEach
    void setUp() {
        hazelcastCommandGateway = new HazelcastCommandGateway(hazelcastInstance, annotationCommandTargetResolver);

        lenient().when(hazelcastInstance.getExecutorService(AXON_COMMAND_DISPATCHER)).thenReturn(executorService);
        lenient().when(annotationCommandTargetResolver.resolveTarget(command))
                .thenReturn(VERSIONED_AGGREGATE_IDENTIFIER);
    }

    @Test
    void sendWillReturnCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<UUID> completableFuture = hazelcastCommandGateway.send(command);

        UUID expectedResponse = randomUUID();
        dispatchCommandAndExecuteCallback(expectedResponse);

        assertTrue(completableFuture.isDone());
        assertFalse(completableFuture.isCompletedExceptionally());
        assertEquals(expectedResponse, completableFuture.get());
    }

    @Test
    void sendWithCallback_WillCallback() {
        final AtomicReference<UUID> callbackResponse = new AtomicReference<>(null);

        UUID expectedResponse = randomUUID();
        hazelcastCommandGateway.send(command, (CommandCallback<CommandMessage<?>, Object>) (message, resultMessage) ->
                callbackResponse.set(expectedResponse));

        dispatchCommandAndExecuteCallback(expectedResponse);
        assertEquals(expectedResponse, callbackResponse.get());
    }

    @Test
    void sendAndWait_WillAwaitCallbackAndReturnResponse_WhenCommandSucceeds() {
        UUID expectedResponse = randomUUID();
        dispatchAndAnswerCallback(expectedResponse);
        assertEquals(expectedResponse, hazelcastCommandGateway.sendAndWait(command));
    }

    @Test
    void sendAndWait_WillAwaitCallbackAndThrowExceptionWrappedAsRuntimeException_WhenCommandFails() {
        doAnswer(invocation -> {
            var expectedException = new org.axonframework.messaging.ExecutionException("Command validation failure", new IllegalStateException("boom"));
            ((AxonDistributableCommandCallback) invocation.getArguments()[2]).onFailure(expectedException);
            return null;
        }).when(executorService).submitToKeyOwner(any(AxonDistributableCommand.class), eq(AGGREGATE_IDENTIFIER), any());

        assertThrows(RuntimeException.class, () -> hazelcastCommandGateway.sendAndWait(command));
    }

    @Test
    void sendAndWaitWithTimeout_WillReceiveCallback() {
        UUID expectedResponse = randomUUID();
        dispatchAndAnswerCallback(expectedResponse);
        assertEquals(expectedResponse, hazelcastCommandGateway.sendAndWait(command, 5, MINUTES));
    }

    @Test
    void sendAndWaitWithTimeout_WillTimeout_WhenCallbackNotReceived() {
        assertThrows(RuntimeException.class, () -> hazelcastCommandGateway.sendAndWait(command, 1, SECONDS));
    }

    @Test
    void registerDispatchInterceptor_WillFail() {
        assertThrows(UnsupportedOperationException.class,
                () -> hazelcastCommandGateway.registerDispatchInterceptor(messageDispatchInterceptor));
    }

    private void dispatchCommandAndExecuteCallback(UUID expectedResponse) {
        ArgumentCaptor<ExecutionCallback<Object>> executionCallbackArgumentCaptor = ArgumentCaptor.forClass(ExecutionCallback.class);
        verify(executorService).submitToKeyOwner(any(AxonDistributableCommand.class), eq(AGGREGATE_IDENTIFIER), executionCallbackArgumentCaptor.capture());
        var executionCallback = executionCallbackArgumentCaptor.getValue();
        executionCallback.onResponse(expectedResponse);
    }

    private void dispatchAndAnswerCallback(UUID expectedResponse) {
        doAnswer(invocation -> {
            ((AxonDistributableCommandCallback) invocation.getArguments()[2]).onResponse(expectedResponse);
            return null;
        }).when(executorService).submitToKeyOwner(any(AxonDistributableCommand.class), eq(AGGREGATE_IDENTIFIER), any());
    }
}