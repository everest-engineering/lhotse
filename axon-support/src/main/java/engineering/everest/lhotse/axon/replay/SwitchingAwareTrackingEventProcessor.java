package engineering.everest.lhotse.axon.replay;

import lombok.experimental.Delegate;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;

public class SwitchingAwareTrackingEventProcessor implements EventProcessor {

    @Delegate(excludes = Resettable.class)
    private final TrackingEventProcessor trackingEventProcessor;
    private final TransactionManager transactionManager;
    private final TokenStore tokenStore;
    private final int initialSegmentsCount;

    public SwitchingAwareTrackingEventProcessor(TrackingEventProcessor trackingEventProcessor,
                                                TransactionManager transactionManager,
                                                TokenStore tokenStore,
                                                int initialSegmentsCount) {
        this.trackingEventProcessor = trackingEventProcessor;
        this.transactionManager = transactionManager;
        this.tokenStore = tokenStore;
        this.initialSegmentsCount = initialSegmentsCount;
    }

    /**
     * Ensure token is at the end of the event stream before actual reset for replay.
     */
    public void resetTokens(TrackingToken startPosition) {
        TrackingToken headToken = getMessageSource().createHeadToken();
        transactionManager.executeInTransaction(() -> {
            int[] segments = tokenStore.fetchSegments(getName());
            if (segments.length > 0) {
                for (int segment : segments) {
                    tokenStore.storeToken(headToken, getName(), segment);
                }
            } else {
                tokenStore.initializeTokenSegments(getName(), initialSegmentsCount, headToken);
            }
        });
        trackingEventProcessor.resetTokens(startPosition);
    }

    private interface Resettable {
        void resetTokens(TrackingToken startPosition);
    }
}
