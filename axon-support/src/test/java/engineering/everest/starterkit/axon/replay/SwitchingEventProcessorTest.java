package engineering.everest.starterkit.axon.replay;

import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SwitchingEventProcessorTest {

    @Mock
    private SubscribingEventProcessor subscribingEventProcessor;
    @Mock
    private TrackingEventProcessor trackingEventProcessor;
    @Mock
    private TrackingToken trackingToken;

    private SwitchingEventProcessor switchingEventProcessor;

    @BeforeEach
    void setUp() {
        switchingEventProcessor = new SwitchingEventProcessor(subscribingEventProcessor, trackingEventProcessor);
    }

    @Test
    void willStartReplay() {
        switchingEventProcessor.startReplay(trackingToken);
        verify(subscribingEventProcessor).shutDown();
        verify(trackingEventProcessor).resetTokens(trackingToken);
        verify(trackingEventProcessor).start();
    }

    @Test
    void willStopReplay() {
        switchingEventProcessor.startReplay(trackingToken);
        switchingEventProcessor.stopReplay();
        trackingEventProcessor.shutDown();
        subscribingEventProcessor.start();
    }

    @Test
    void willGetReplayingStatus() {
        assertFalse(switchingEventProcessor.isRelaying());
        switchingEventProcessor.startReplay(trackingToken);
        assertTrue(switchingEventProcessor.isRelaying());
    }

}
