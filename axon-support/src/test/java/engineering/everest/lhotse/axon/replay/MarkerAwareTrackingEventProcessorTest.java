package engineering.everest.lhotse.axon.replay;

import engineering.everest.lhotse.axon.config.AxonConfig;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.ErrorHandler;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventTrackerStatus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.Segment;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.unitofwork.RollbackConfiguration;
import org.axonframework.monitoring.MessageMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkerAwareTrackingEventProcessorTest {

    @Mock
    private Configuration configuration;
    @Mock
    private EventProcessingModule eventProcessingModule;
    @Mock
    private EventHandlerInvoker eventHandlerInvoker;
    @Mock
    private EmbeddedEventStore embeddedEventStore;
    @Mock
    private TokenStore tokenStore;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private TrackingToken startPosition;
    @Mock
    private ReplayMarkerEvent replayMarkerEvent;

    private MarkerAwareTrackingEventProcessor processor;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> ((Supplier) invocation.getArgument(1)).get())
                .when(configuration).getComponent(any(), any());
        when(eventProcessingModule.rollbackConfiguration(any())).thenReturn(mock(RollbackConfiguration.class));
        when(eventProcessingModule.errorHandler(any())).thenReturn(mock(ErrorHandler.class));
        when(eventProcessingModule.messageMonitor(any(), any())).thenReturn(mock(MessageMonitor.class));
        when(configuration.eventBus()).thenReturn(embeddedEventStore);
        when(eventProcessingModule.tokenStore(any())).thenReturn(tokenStore);
        when(eventProcessingModule.transactionManager(any())).thenReturn(transactionManager);
        when(eventHandlerInvoker.supportsReset()).thenReturn(true);
        processor = (MarkerAwareTrackingEventProcessor)
                new CompositeEventProcessorBuilder(
                        eventProcessingModule, AxonConfig.EventProcessorType.TRACKING, 2)
                        .build("default", configuration, eventHandlerInvoker);
    }

    @Test
    void startReplayWillWorkAsSwitchingAware() throws Exception {
        Field switchingAwareField = processor.getClass().getDeclaredField("switchingAware");
        switchingAwareField.setAccessible(true);
        switchingAwareField.setBoolean(processor, true);

        MarkerAwareTrackingEventProcessor markerAwareTrackingEventProcessor = spy(processor);
        when(markerAwareTrackingEventProcessor.processingStatus()).thenReturn(Map.of(
                0, mock(EventTrackerStatus.class),
                1, mock(EventTrackerStatus.class)));

        SubscribingEventProcessor subscribingEventProcessor = mock(SubscribingEventProcessor.class);
        SwitchingEventProcessor switchingEventProcessor =
                new SwitchingEventProcessor(subscribingEventProcessor, markerAwareTrackingEventProcessor);

        CountDownLatch replayLatch = new CountDownLatch(1);
        Consumer<ReplayableEventProcessor> listener = p -> replayLatch.countDown();
        switchingEventProcessor.registerReplayCompletionListener(listener);
        switchingEventProcessor.startReplay(startPosition, replayMarkerEvent);

        verify(subscribingEventProcessor).shutDown();
        verify(embeddedEventStore).createHeadToken();
        verify(markerAwareTrackingEventProcessor).resetTokens(startPosition);
        verify(markerAwareTrackingEventProcessor).start();
        assertTrue(switchingEventProcessor.isReplaying());

        // Feed the replay event
        markerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
                List.of(mock(Segment.class)));
        // one is not enough since we have two segments
        assertTrue(switchingEventProcessor.isReplaying());
        assertEquals(1, replayLatch.getCount());

        // Feed again the replay event
        markerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
                List.of(mock(Segment.class)));
        // Now the replay be completed
        replayLatch.await(1, TimeUnit.SECONDS);
        assertEquals(0, replayLatch.getCount());
        assertFalse(switchingEventProcessor.isReplaying());
    }

    @Test
    void startReplayWillWorkStandalone() throws Exception {
        MarkerAwareTrackingEventProcessor markerAwareTrackingEventProcessor = spy(processor);
        when(markerAwareTrackingEventProcessor.processingStatus()).thenReturn(Map.of(
                0, mock(EventTrackerStatus.class),
                1, mock(EventTrackerStatus.class)));
        CountDownLatch replayLatch = new CountDownLatch(1);
        Consumer<ReplayableEventProcessor> listener = p -> replayLatch.countDown();
        markerAwareTrackingEventProcessor.registerReplayCompletionListener(listener);
        markerAwareTrackingEventProcessor.startReplay(startPosition, replayMarkerEvent);
        assertTrue(markerAwareTrackingEventProcessor.isReplaying());
        verify(markerAwareTrackingEventProcessor).shutDown();
        verify(markerAwareTrackingEventProcessor).resetTokens(startPosition);
        verify(markerAwareTrackingEventProcessor).start();
        assertTrue(markerAwareTrackingEventProcessor.isReplaying());

        // Feed the replay event
        markerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
                List.of(mock(Segment.class)));
        // one is not enough since we have two segments
        assertTrue(markerAwareTrackingEventProcessor.isReplaying());
        assertEquals(1, replayLatch.getCount());

        // Feed again the replay event
        markerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
                List.of(mock(Segment.class)));
        // Now the replay be completed
        replayLatch.await(1, TimeUnit.SECONDS);
        assertEquals(0, replayLatch.getCount());
        assertFalse(markerAwareTrackingEventProcessor.isReplaying());
    }
}
