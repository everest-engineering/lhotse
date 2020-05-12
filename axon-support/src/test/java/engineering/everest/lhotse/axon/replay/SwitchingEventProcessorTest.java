package engineering.everest.lhotse.axon.replay;

import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.lifecycle.ShutdownHandler;
import org.axonframework.lifecycle.StartHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwitchingEventProcessorTest {

    @Mock
    private SubscribingEventProcessor subscribingEventProcessor;
    @Mock
    private MarkerAwareTrackingEventProcessor markerAwareTrackingEventProcessor;
    @Mock
    private TrackingToken startPosition;
    @Mock
    private ReplayMarkerEvent replayMarkerEvent;

    private SwitchingEventProcessor switchingEventProcessor;

    @BeforeEach
    void setUp() {
        switchingEventProcessor = new SwitchingEventProcessor(
                subscribingEventProcessor, markerAwareTrackingEventProcessor);
    }

    @Test
    void willStartReplay() {
        switchingEventProcessor.startReplay(startPosition, replayMarkerEvent);
        verify(subscribingEventProcessor).shutDown();
        verify(markerAwareTrackingEventProcessor).startReplay(startPosition, replayMarkerEvent);
    }

    @Test
    void willGetReplayingStatus() {
        assertFalse(switchingEventProcessor.isReplaying());
        switchingEventProcessor.startReplay(startPosition, replayMarkerEvent);
        assertTrue(switchingEventProcessor.isReplaying());
    }

    @Test
    void getName_WillDelegate() {
        when(subscribingEventProcessor.getName()).thenReturn("Jim");

        assertEquals("Jim", switchingEventProcessor.getName());
    }

    @Test
    void start_IsRegisteredAsAnStartupHandler() throws NoSuchMethodException {
        Method startMethod = SwitchingEventProcessor.class.getDeclaredMethod("start");
        assertTrue(startMethod.isAnnotationPresent(StartHandler.class));
    }

    @Test
    void start_WillDelegate() {
        switchingEventProcessor.start();

        verify(subscribingEventProcessor).start();
    }

    @Test
    void shutdown_IsRegisteredAsAShutdownHandler() throws NoSuchMethodException {
        Method startMethod = SwitchingEventProcessor.class.getDeclaredMethod("shutDown");
        assertTrue(startMethod.isAnnotationPresent(ShutdownHandler.class));
    }

    @Test
    void shutdown_WillDelegate() {
        switchingEventProcessor.shutDown();

        verify(subscribingEventProcessor).shutDown();
    }
}
