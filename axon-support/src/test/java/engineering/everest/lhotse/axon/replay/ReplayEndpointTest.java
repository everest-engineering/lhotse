package engineering.everest.lhotse.axon.replay;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplayEndpointTest {

    @Mock
    private AxonConfiguration axonConfiguration;
    @Mock
    private ReplayPreparation replayPreparation;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private EventProcessingConfiguration eventProcessingConfiguration;
    @Mock
    private SwitchingEventProcessor switchingEventProcessor;
    @Mock
    private EventStore eventStore;
    @Mock
    private EventGateway eventGateway;
    @Mock
    private TrackingToken trackingToken;

    private ReplayEndpoint replayEndpoint;

    @BeforeEach
    void setUp() {
        lenient().when(axonConfiguration.eventProcessingConfiguration()).thenReturn(eventProcessingConfiguration);
        lenient().when(eventProcessingConfiguration.eventProcessors()).thenReturn(Map.of("default", switchingEventProcessor));
        lenient().when(switchingEventProcessor.isRelaying()).thenReturn(false);
        replayEndpoint = new ReplayEndpoint(axonConfiguration, List.of(replayPreparation), taskExecutor);
    }

    @Test
    void willGetReplayStatus() {
        Map<String, Object> status = replayEndpoint.status();
        assertEquals(Map.of("switchingEventProcessors", 1, "isReplaying", false), status);
    }

    @Test
    void willStartReplay() {
        when(axonConfiguration.eventStore()).thenReturn(eventStore);
        when(axonConfiguration.eventGateway()).thenReturn(eventGateway);
        when(eventStore.createTailToken()).thenReturn(trackingToken);
        replayEndpoint.startReplay(null, null);
        verify(switchingEventProcessor).startReplay(trackingToken);
        verify(eventGateway).publish(any(ReplayMarkerEvent.class));
    }

    @Test
    void triggerReplayWillThrowIllegalStateException_WhenReplayingIsOngoing() {
        when(switchingEventProcessor.isRelaying()).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> replayEndpoint.startReplay(null, null));
    }

    @Test
    void triggerReplayWillThrowIllegalStateException_WhenNoMatchingSwitchingEventProcessorFound() {
        when(eventProcessingConfiguration.eventProcessorByProcessingGroup("foo", SwitchingEventProcessor.class)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> replayEndpoint.startReplay(Set.of("foo"), null));
    }

    @Test
    void onResetWillRunPreparations() {
        replayEndpoint.onReset();
        verify(replayPreparation).run();
    }

    @Test
    void onReplayMarkerEventWillStopReplay() {
        when(switchingEventProcessor.isRelaying()).thenReturn(true);
        doAnswer(invocation -> {
            ((Runnable)invocation.getArgument(0)).run();
            return null;
        }).when(taskExecutor).execute(any());
        replayEndpoint.on(new ReplayMarkerEvent(randomUUID()));
        verify(switchingEventProcessor).stopReplay();
    }

    @Test
    void onReplayMarkerEventWillIgnore_WhenReplayIsNotRunning() {
        replayEndpoint.on(new ReplayMarkerEvent(randomUUID()));
        verify(switchingEventProcessor, never()).stopReplay();
    }


}
