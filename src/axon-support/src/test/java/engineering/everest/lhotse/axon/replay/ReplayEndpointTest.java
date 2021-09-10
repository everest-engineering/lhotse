package engineering.everest.lhotse.axon.replay;

import engineering.everest.lhotse.axon.replay.ReplayableEventProcessor.ListenerRegistry;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.spring.config.AxonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplayEndpointTest {

    @Mock
    private AxonConfiguration axonConfiguration;
    @Mock
    private ReplayCompletionAware replayCompletionAware;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private EventProcessingConfiguration eventProcessingConfiguration;
    @Mock
    private ReplayMarkerAwareTrackingEventProcessor replayMarkerAwareTrackingEventProcessor;
    @Mock
    private EventStore eventStore;
    @Mock
    private EventGateway eventGateway;
    @Mock
    private TrackingToken startPosition;
    @Mock
    private ListenerRegistry listenerRegistry;

    private ReplayEndpoint replayEndpoint;

    @BeforeEach
    void setUp() {
        lenient().when(axonConfiguration.eventProcessingConfiguration()).thenReturn(eventProcessingConfiguration);
        lenient().when(eventProcessingConfiguration.eventProcessors()).thenReturn(Map.of("default",
                replayMarkerAwareTrackingEventProcessor));
        lenient().when(replayMarkerAwareTrackingEventProcessor.isReplaying()).thenReturn(false);
        replayEndpoint = new ReplayEndpoint(axonConfiguration, List.of(replayCompletionAware), taskExecutor);
    }

    @Test
    void willGetReplayStatus() {
        Map<String, Object> status = replayEndpoint.status();
        assertEquals(Map.of("ReplayableEventProcessors", 1,
                "currentlyReplaying", 0,
                "isReplaying", false), status);
    }

    @Test
    void willStartReplay() throws IOException {
        when(axonConfiguration.eventStore()).thenReturn(eventStore);
        when(axonConfiguration.eventGateway()).thenReturn(eventGateway);
        when(eventStore.createTailToken()).thenReturn(startPosition);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(taskExecutor).execute(any());
        AtomicReference<Consumer<ReplayableEventProcessor>> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(0));
            return listenerRegistry;
        }).when(replayMarkerAwareTrackingEventProcessor).registerReplayCompletionListener(any());
        replayEndpoint.startReplay(null, null);
        verify(replayMarkerAwareTrackingEventProcessor).startReplay(eq(startPosition), any(ReplayMarkerEvent.class));
        verify(eventGateway).publish(any(ReplayMarkerEvent.class));

        // Another attempt to start replay before current one is completed will fail
        assertThrows(IllegalStateException.class, () -> replayEndpoint.startReplay(null, null));
        Map<String, Object> status = replayEndpoint.status();
        // Status should show currently replaying processors
        assertEquals(Map.of("ReplayableEventProcessors", 1,
                "currentlyReplaying", 1,
                "isReplaying", true), status);

        // Now complete the replay
        listener.get().accept(replayMarkerAwareTrackingEventProcessor);
        verify(listenerRegistry).close();
        verify(replayCompletionAware).replayCompleted();
    }

    @Test
    void triggerReplayWillThrowIllegalStateException_WhenNoMatchingEventProcessorFound() {
        when(eventProcessingConfiguration.eventProcessorByProcessingGroup(
                "foo", ReplayableEventProcessor.class)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> replayEndpoint.startReplay(Set.of("foo"), null));
    }
}
