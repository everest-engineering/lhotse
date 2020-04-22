package engineering.everest.lhotse.axon.replay;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.StreamableMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwitchingAwareTrackingEventProcessorTest {

    private final String PROCESSOR_NAME = "default";

    @Mock
    private TrackingEventProcessor trackingEventProcessor;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private TokenStore tokenStore;
    @Mock
    private StreamableMessageSource<? extends TrackedEventMessage<?>> streamableMessageSource;
    @Mock
    private TrackingToken headToken;
    @Mock
    private TrackingToken startPosition;

    private SwitchingAwareTrackingEventProcessor switchingAwareTrackingEventProcessor;

    @BeforeEach
    void setUp() {
        switchingAwareTrackingEventProcessor = new SwitchingAwareTrackingEventProcessor(
                trackingEventProcessor,
                transactionManager,
                tokenStore,
                1
        );
        when(switchingAwareTrackingEventProcessor.getName()).thenReturn(PROCESSOR_NAME);
        Mockito.<StreamableMessageSource<? extends TrackedEventMessage<?>>>when(
                switchingAwareTrackingEventProcessor.getMessageSource()).thenReturn(streamableMessageSource);
        when(streamableMessageSource.createHeadToken()).thenReturn(headToken);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(transactionManager).executeInTransaction(isA(Runnable.class));
    }

    @Test
    void resetTokensWillInitialiseSegmentsAndDelegate() {
        when(tokenStore.fetchSegments(PROCESSOR_NAME)).thenReturn(new int[0]);
        switchingAwareTrackingEventProcessor.resetTokens(startPosition);
        verify(tokenStore).initializeTokenSegments(PROCESSOR_NAME, 1, headToken);
        verify(trackingEventProcessor).resetTokens(startPosition);
    }

    @Test
    void resetTokensWillStoreHeadTokenAndDelegate() {
        when(tokenStore.fetchSegments(PROCESSOR_NAME)).thenReturn(new int[]{0});
        switchingAwareTrackingEventProcessor.resetTokens(startPosition);
        verify(tokenStore).storeToken(headToken, PROCESSOR_NAME, 0);
        verify(trackingEventProcessor).resetTokens(startPosition);
    }
}
