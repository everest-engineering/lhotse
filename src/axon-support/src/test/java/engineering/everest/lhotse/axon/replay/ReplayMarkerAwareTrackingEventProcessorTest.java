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
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplayMarkerAwareTrackingEventProcessorTest {

    @Mock
    private Configuration configuration;
    @Mock
    private EventProcessingModule eventProcessingModule;
    @Mock
    private TaskExecutor taskExecutor;
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

    private ReplayMarkerAwareTrackingEventProcessor processor;

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
        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
        processor = (ReplayMarkerAwareTrackingEventProcessor) new CompositeEventProcessorBuilder(
            taskExecutor, eventProcessingModule, AxonConfig.EventProcessorType.TRACKING, 2)
                .build("default", configuration, eventHandlerInvoker);
    }

    @Test
    void startReplay_WillFail_WhenReplayAlreadyInProgress() {
        var replayMarkerAwareTrackingEventProcessor = spy(processor);
        replayMarkerAwareTrackingEventProcessor.startReplay(startPosition, replayMarkerEvent);

        var thrownException = assertThrows(RuntimeException.class,
            () -> replayMarkerAwareTrackingEventProcessor.startReplay(startPosition, replayMarkerEvent));
        assertEquals("Previous replay is still running", thrownException.getMessage());
    }

    @Test
    void startReplay_WillWorkStandalone() throws Exception {
        var replayMarkerAwareTrackingEventProcessor = spy(processor);
        when(replayMarkerAwareTrackingEventProcessor.processingStatus()).thenReturn(Map.of(
            0, mock(EventTrackerStatus.class),
            1, mock(EventTrackerStatus.class)));
        CountDownLatch replayLatch = new CountDownLatch(1);
        Consumer<ReplayableEventProcessor> listener = p -> replayLatch.countDown();
        replayMarkerAwareTrackingEventProcessor.registerReplayCompletionListener(listener);
        replayMarkerAwareTrackingEventProcessor.startReplay(startPosition, replayMarkerEvent);
        assertTrue(replayMarkerAwareTrackingEventProcessor.isReplaying());
        verify(replayMarkerAwareTrackingEventProcessor).shutDown();
        verify(replayMarkerAwareTrackingEventProcessor).resetTokens(startPosition);
        verify(replayMarkerAwareTrackingEventProcessor).start();
        assertTrue(replayMarkerAwareTrackingEventProcessor.isReplaying());

        // Feed the replay event
        replayMarkerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
            List.of(mock(Segment.class)));
        // one is not enough since we have two segments
        assertTrue(replayMarkerAwareTrackingEventProcessor.isReplaying());
        assertEquals(1, replayLatch.getCount());

        // Feed again the replay event
        replayMarkerAwareTrackingEventProcessor.canHandle(new GenericEventMessage<>(replayMarkerEvent),
            List.of(mock(Segment.class)));
        // Now the replay be completed
        replayLatch.await(1, TimeUnit.SECONDS);
        assertEquals(0, replayLatch.getCount());
        assertFalse(replayMarkerAwareTrackingEventProcessor.isReplaying());
    }
}
